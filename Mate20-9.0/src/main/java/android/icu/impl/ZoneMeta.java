package android.icu.impl;

import android.icu.text.NumberFormat;
import android.icu.util.Output;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeZone;
import android.icu.util.UResourceBundle;
import dalvik.system.VMRuntime;
import java.lang.ref.SoftReference;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;

public final class ZoneMeta {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean ASSERT = false;
    private static ICUCache<String, String> CANONICAL_ID_CACHE = new SimpleCache();
    private static final CustomTimeZoneCache CUSTOM_ZONE_CACHE = new CustomTimeZoneCache();
    private static SoftReference<Set<String>> REF_CANONICAL_SYSTEM_LOCATION_ZONES = null;
    private static SoftReference<Set<String>> REF_CANONICAL_SYSTEM_ZONES = null;
    private static SoftReference<Set<String>> REF_SYSTEM_ZONES = null;
    private static ICUCache<String, String> REGION_CACHE = new SimpleCache();
    private static ICUCache<String, Boolean> SINGLE_COUNTRY_CACHE = new SimpleCache();
    private static final SystemTimeZoneCache SYSTEM_ZONE_CACHE = new SystemTimeZoneCache();
    private static String[] ZONEIDS = null;
    private static final String ZONEINFORESNAME = "zoneinfo64";
    private static final String kCUSTOM_TZ_PREFIX = "GMT";
    private static final String kGMT_ID = "GMT";
    private static final int kMAX_CUSTOM_HOUR = 23;
    private static final int kMAX_CUSTOM_MIN = 59;
    private static final int kMAX_CUSTOM_SEC = 59;
    private static final String kNAMES = "Names";
    private static final String kREGIONS = "Regions";
    private static final String kWorld = "001";
    private static final String kZONES = "Zones";

    private static class CustomTimeZoneCache extends SoftCache<Integer, SimpleTimeZone, int[]> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<ZoneMeta> cls = ZoneMeta.class;
        }

        private CustomTimeZoneCache() {
        }

        /* access modifiers changed from: protected */
        public SimpleTimeZone createInstance(Integer key, int[] data) {
            SimpleTimeZone tz = new SimpleTimeZone(data[0] * ((((data[1] * 60) + data[2]) * 60) + data[3]) * 1000, ZoneMeta.formatCustomID(data[1], data[2], data[3], data[0] < 0));
            tz.freeze();
            return tz;
        }
    }

    private static class SystemTimeZoneCache extends SoftCache<String, OlsonTimeZone, String> {
        private SystemTimeZoneCache() {
        }

        /* access modifiers changed from: protected */
        public OlsonTimeZone createInstance(String key, String data) {
            try {
                UResourceBundle top = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZoneMeta.ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                UResourceBundle res = ZoneMeta.openOlsonResource(top, data);
                if (res == null) {
                    return null;
                }
                OlsonTimeZone tz = new OlsonTimeZone(top, res, data);
                tz.freeze();
                return tz;
            } catch (MissingResourceException e) {
                return null;
            }
        }
    }

    private static synchronized Set<String> getSystemZIDs() {
        Set<String> systemZones;
        synchronized (ZoneMeta.class) {
            systemZones = null;
            if (REF_SYSTEM_ZONES != null) {
                systemZones = REF_SYSTEM_ZONES.get();
            }
            if (systemZones == null) {
                Set<String> systemIDs = new TreeSet<>();
                for (String id : getZoneIDs()) {
                    if (!id.equals(TimeZone.UNKNOWN_ZONE_ID)) {
                        systemIDs.add(id);
                    }
                }
                systemZones = Collections.unmodifiableSet(systemIDs);
                REF_SYSTEM_ZONES = new SoftReference<>(systemZones);
            }
        }
        return systemZones;
    }

    private static synchronized Set<String> getCanonicalSystemZIDs() {
        Set<String> canonicalSystemZones;
        synchronized (ZoneMeta.class) {
            canonicalSystemZones = null;
            if (REF_CANONICAL_SYSTEM_ZONES != null) {
                canonicalSystemZones = REF_CANONICAL_SYSTEM_ZONES.get();
            }
            if (canonicalSystemZones == null) {
                Set<String> canonicalSystemIDs = new TreeSet<>();
                for (String id : getZoneIDs()) {
                    if (!id.equals(TimeZone.UNKNOWN_ZONE_ID)) {
                        if (id.equals(getCanonicalCLDRID(id))) {
                            canonicalSystemIDs.add(id);
                        }
                    }
                }
                canonicalSystemZones = Collections.unmodifiableSet(canonicalSystemIDs);
                REF_CANONICAL_SYSTEM_ZONES = new SoftReference<>(canonicalSystemZones);
            }
        }
        return canonicalSystemZones;
    }

    private static synchronized Set<String> getCanonicalSystemLocationZIDs() {
        Set<String> canonicalSystemLocationZones;
        synchronized (ZoneMeta.class) {
            canonicalSystemLocationZones = null;
            if (REF_CANONICAL_SYSTEM_LOCATION_ZONES != null) {
                canonicalSystemLocationZones = REF_CANONICAL_SYSTEM_LOCATION_ZONES.get();
            }
            if (canonicalSystemLocationZones == null) {
                Set<String> canonicalSystemLocationIDs = new TreeSet<>();
                for (String id : getZoneIDs()) {
                    if (!id.equals(TimeZone.UNKNOWN_ZONE_ID)) {
                        if (id.equals(getCanonicalCLDRID(id))) {
                            String region = getRegion(id);
                            if (region != null && !region.equals(kWorld)) {
                                canonicalSystemLocationIDs.add(id);
                            }
                        }
                    }
                }
                canonicalSystemLocationZones = Collections.unmodifiableSet(canonicalSystemLocationIDs);
                REF_CANONICAL_SYSTEM_LOCATION_ZONES = new SoftReference<>(canonicalSystemLocationZones);
            }
        }
        return canonicalSystemLocationZones;
    }

    public static Set<String> getAvailableIDs(TimeZone.SystemTimeZoneType type, String region, Integer rawOffset) {
        Set<String> baseSet;
        switch (type) {
            case ANY:
                baseSet = getSystemZIDs();
                break;
            case CANONICAL:
                baseSet = getCanonicalSystemZIDs();
                break;
            case CANONICAL_LOCATION:
                baseSet = getCanonicalSystemLocationZIDs();
                break;
            default:
                throw new IllegalArgumentException("Unknown SystemTimeZoneType");
        }
        if (region == null && rawOffset == null) {
            return baseSet;
        }
        if (region != null) {
            region = region.toUpperCase(Locale.ENGLISH);
        }
        Set<String> result = new TreeSet<>();
        for (String id : baseSet) {
            if (region == null || region.equals(getRegion(id))) {
                if (rawOffset != null) {
                    TimeZone z = getSystemTimeZone(id);
                    if (z != null) {
                        if (!rawOffset.equals(Integer.valueOf(z.getRawOffset()))) {
                        }
                    }
                }
                result.add(id);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(result);
    }

    public static synchronized int countEquivalentIDs(String id) {
        int count;
        synchronized (ZoneMeta.class) {
            count = 0;
            UResourceBundle res = openOlsonResource(null, id);
            if (res != null) {
                try {
                    count = res.get("links").getIntVector().length;
                } catch (MissingResourceException e) {
                }
            }
        }
        return count;
    }

    public static synchronized String getEquivalentID(String id, int index) {
        String result;
        synchronized (ZoneMeta.class) {
            result = "";
            if (index >= 0) {
                UResourceBundle res = openOlsonResource(null, id);
                if (res != null) {
                    int zoneIdx = -1;
                    try {
                        int[] zones = res.get("links").getIntVector();
                        if (index < zones.length) {
                            zoneIdx = zones[index];
                        }
                    } catch (MissingResourceException e) {
                    }
                    if (zoneIdx >= 0) {
                        String tmp = getZoneID(zoneIdx);
                        if (tmp != null) {
                            result = tmp;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static synchronized String[] getZoneIDs() {
        String[] strArr;
        synchronized (ZoneMeta.class) {
            if (ZONEIDS == null) {
                try {
                    ZONEIDS = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER).getStringArray(kNAMES);
                } catch (MissingResourceException e) {
                }
            }
            if (ZONEIDS == null) {
                ZONEIDS = new String[0];
            }
            strArr = ZONEIDS;
        }
        return strArr;
    }

    private static String getZoneID(int idx) {
        if (idx >= 0) {
            String[] ids = getZoneIDs();
            if (idx < ids.length) {
                return ids[idx];
            }
        }
        return null;
    }

    private static int getZoneIndex(String zid) {
        String[] all = getZoneIDs();
        if (all.length <= 0) {
            return -1;
        }
        int start = 0;
        int limit = all.length;
        int lastMid = Integer.MAX_VALUE;
        while (true) {
            int mid = (start + limit) / 2;
            if (lastMid == mid) {
                return -1;
            }
            lastMid = mid;
            int r = zid.compareTo(all[mid]);
            if (r == 0) {
                return mid;
            }
            if (r < 0) {
                limit = mid;
            } else {
                start = mid;
            }
        }
    }

    public static String getCanonicalCLDRID(TimeZone tz) {
        if (tz instanceof OlsonTimeZone) {
            return ((OlsonTimeZone) tz).getCanonicalID();
        }
        return getCanonicalCLDRID(tz.getID());
    }

    public static String getCanonicalCLDRID(String tzid) {
        String canonical = CANONICAL_ID_CACHE.get(tzid);
        if (canonical == null) {
            canonical = findCLDRCanonicalID(tzid);
            if (canonical == null) {
                try {
                    int zoneIdx = getZoneIndex(tzid);
                    if (zoneIdx >= 0) {
                        UResourceBundle zone = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER).get(kZONES).get(zoneIdx);
                        if (zone.getType() == 7) {
                            tzid = getZoneID(zone.getInt());
                            canonical = findCLDRCanonicalID(tzid);
                        }
                        if (canonical == null) {
                            canonical = tzid;
                        }
                    }
                } catch (MissingResourceException e) {
                }
            }
            if (canonical != null) {
                CANONICAL_ID_CACHE.put(tzid, canonical);
            }
        }
        return canonical;
    }

    private static String findCLDRCanonicalID(String tzid) {
        String canonical = null;
        String tzidKey = tzid.replace('/', ':');
        try {
            UResourceBundle keyTypeData = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            try {
                keyTypeData.get("typeMap").get("timezone").get(tzidKey);
                canonical = tzid;
            } catch (MissingResourceException e) {
            }
            if (canonical == null) {
                return keyTypeData.get("typeAlias").get("timezone").getString(tzidKey);
            }
            return canonical;
        } catch (MissingResourceException e2) {
            return canonical;
        }
    }

    public static String getRegion(String tzid) {
        String region = REGION_CACHE.get(tzid);
        if (region == null) {
            int zoneIdx = getZoneIndex(tzid);
            if (zoneIdx >= 0) {
                try {
                    UResourceBundle regions = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER).get(kREGIONS);
                    if (zoneIdx < regions.getSize()) {
                        region = regions.getString(zoneIdx);
                    }
                } catch (MissingResourceException e) {
                }
                if (region != null) {
                    REGION_CACHE.put(tzid, region);
                }
            }
        }
        return region;
    }

    public static String getCanonicalCountry(String tzid) {
        String country = getRegion(tzid);
        if (country == null || !country.equals(kWorld)) {
            return country;
        }
        return null;
    }

    public static String getCanonicalCountry(String tzid, Output<Boolean> isPrimary) {
        isPrimary.value = Boolean.FALSE;
        String country = getRegion(tzid);
        if (country != null && country.equals(kWorld)) {
            return null;
        }
        Boolean singleZone = SINGLE_COUNTRY_CACHE.get(tzid);
        if (singleZone == null) {
            boolean z = true;
            if (TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.CANONICAL_LOCATION, country, null).size() > 1) {
                z = false;
            }
            singleZone = Boolean.valueOf(z);
            SINGLE_COUNTRY_CACHE.put(tzid, singleZone);
        }
        if (singleZone.booleanValue()) {
            isPrimary.value = Boolean.TRUE;
        } else {
            try {
                String primaryZone = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones").get("primaryZones").getString(country);
                if (tzid.equals(primaryZone)) {
                    isPrimary.value = Boolean.TRUE;
                } else {
                    String canonicalID = getCanonicalCLDRID(tzid);
                    if (canonicalID != null && canonicalID.equals(primaryZone)) {
                        isPrimary.value = Boolean.TRUE;
                    }
                }
            } catch (MissingResourceException e) {
            }
        }
        return country;
    }

    public static UResourceBundle openOlsonResource(UResourceBundle top, String id) {
        int zoneIdx = getZoneIndex(id);
        if (zoneIdx < 0) {
            return null;
        }
        if (top == null) {
            try {
                top = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            } catch (MissingResourceException e) {
                return null;
            }
        }
        UResourceBundle zones = top.get(kZONES);
        UResourceBundle zone = zones.get(zoneIdx);
        if (zone.getType() == 7) {
            zone = zones.get(zone.getInt());
        }
        return zone;
    }

    public static OlsonTimeZone getSystemTimeZone(String id) {
        return (OlsonTimeZone) SYSTEM_ZONE_CACHE.getInstance(id, id);
    }

    public static SimpleTimeZone getCustomTimeZone(String id) {
        int[] fields = new int[4];
        if (!parseCustomID(id, fields)) {
            return null;
        }
        return (SimpleTimeZone) CUSTOM_ZONE_CACHE.getInstance(Integer.valueOf(fields[0] * (fields[1] | (fields[2] << 5) | (fields[3] << 11))), fields);
    }

    public static String getCustomID(String id) {
        int[] fields = new int[4];
        if (!parseCustomID(id, fields)) {
            return null;
        }
        boolean z = true;
        int i = fields[1];
        int i2 = fields[2];
        int i3 = fields[3];
        if (fields[0] >= 0) {
            z = false;
        }
        return formatCustomID(i, i2, i3, z);
    }

    static boolean parseCustomID(String id, int[] fields) {
        String str = id;
        int[] iArr = fields;
        if (str != null && id.length() > "GMT".length() && str.toUpperCase(Locale.ENGLISH).startsWith("GMT")) {
            ParsePosition pos = new ParsePosition("GMT".length());
            int sign = 1;
            int min = 0;
            int sec = 0;
            if (str.charAt(pos.getIndex()) == '-') {
                sign = -1;
            } else if (str.charAt(pos.getIndex()) != '+') {
                return false;
            }
            pos.setIndex(pos.getIndex() + 1);
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setParseIntegerOnly(true);
            int start = pos.getIndex();
            Number n = numberFormat.parse(str, pos);
            if (pos.getIndex() == start) {
                return false;
            }
            int hour = n.intValue();
            if (pos.getIndex() >= id.length()) {
                int length = pos.getIndex() - start;
                if (length > 0 && 6 >= length) {
                    switch (length) {
                        case 3:
                        case 4:
                            min = hour % 100;
                            hour /= 100;
                            break;
                        case 5:
                        case 6:
                            sec = hour % 100;
                            min = (hour / 100) % 100;
                            hour /= VMRuntime.SDK_VERSION_CUR_DEVELOPMENT;
                            break;
                    }
                } else {
                    return false;
                }
            } else if (pos.getIndex() - start > 2 || str.charAt(pos.getIndex()) != ':') {
                return false;
            } else {
                pos.setIndex(pos.getIndex() + 1);
                int oldPos = pos.getIndex();
                Number n2 = numberFormat.parse(str, pos);
                if (pos.getIndex() - oldPos != 2) {
                    return false;
                }
                min = n2.intValue();
                if (pos.getIndex() < id.length()) {
                    if (str.charAt(pos.getIndex()) != ':') {
                        return false;
                    }
                    pos.setIndex(pos.getIndex() + 1);
                    int oldPos2 = pos.getIndex();
                    Number n3 = numberFormat.parse(str, pos);
                    if (pos.getIndex() != id.length() || pos.getIndex() - oldPos2 != 2) {
                        return false;
                    }
                    sec = n3.intValue();
                }
            }
            if (hour <= 23 && min <= 59 && sec <= 59) {
                if (iArr != null) {
                    if (iArr.length >= 1) {
                        iArr[0] = sign;
                    }
                    if (iArr.length >= 2) {
                        iArr[1] = hour;
                    }
                    if (iArr.length >= 3) {
                        iArr[2] = min;
                    }
                    if (iArr.length >= 4) {
                        iArr[3] = sec;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static SimpleTimeZone getCustomTimeZone(int offset) {
        boolean negative = false;
        int tmp = offset;
        if (offset < 0) {
            negative = true;
            tmp = -offset;
        }
        int tmp2 = tmp / 1000;
        int sec = tmp2 % 60;
        int tmp3 = tmp2 / 60;
        return new SimpleTimeZone(offset, formatCustomID(tmp3 / 60, tmp3 % 60, sec, negative));
    }

    static String formatCustomID(int hour, int min, int sec, boolean negative) {
        StringBuilder zid = new StringBuilder("GMT");
        if (!(hour == 0 && min == 0)) {
            if (negative) {
                zid.append('-');
            } else {
                zid.append('+');
            }
            if (hour < 10) {
                zid.append('0');
            }
            zid.append(hour);
            zid.append(':');
            if (min < 10) {
                zid.append('0');
            }
            zid.append(min);
            if (sec != 0) {
                zid.append(':');
                if (sec < 10) {
                    zid.append('0');
                }
                zid.append(sec);
            }
        }
        return zid.toString();
    }

    public static String getShortID(TimeZone tz) {
        String canonicalID;
        if (tz instanceof OlsonTimeZone) {
            canonicalID = ((OlsonTimeZone) tz).getCanonicalID();
        } else {
            canonicalID = getCanonicalCLDRID(tz.getID());
        }
        if (canonicalID == null) {
            return null;
        }
        return getShortIDFromCanonical(canonicalID);
    }

    public static String getShortID(String id) {
        String canonicalID = getCanonicalCLDRID(id);
        if (canonicalID == null) {
            return null;
        }
        return getShortIDFromCanonical(canonicalID);
    }

    private static String getShortIDFromCanonical(String canonicalID) {
        try {
            return UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("typeMap").get("timezone").getString(canonicalID.replace('/', ':'));
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
