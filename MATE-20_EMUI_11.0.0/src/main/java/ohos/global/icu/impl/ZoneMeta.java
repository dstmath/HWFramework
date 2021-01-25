package ohos.global.icu.impl;

import java.lang.ref.SoftReference;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.SimpleTimeZone;
import ohos.global.icu.util.TimeZone;
import ohos.global.icu.util.UResourceBundle;

public final class ZoneMeta {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean ASSERT = false;
    private static ICUCache<String, String> CANONICAL_ID_CACHE = new SimpleCache();
    private static final CustomTimeZoneCache CUSTOM_ZONE_CACHE = new CustomTimeZoneCache(null);
    private static SoftReference<Set<String>> REF_CANONICAL_SYSTEM_LOCATION_ZONES = null;
    private static SoftReference<Set<String>> REF_CANONICAL_SYSTEM_ZONES = null;
    private static SoftReference<Set<String>> REF_SYSTEM_ZONES = null;
    private static ICUCache<String, String> REGION_CACHE = new SimpleCache();
    private static ICUCache<String, Boolean> SINGLE_COUNTRY_CACHE = new SimpleCache();
    private static final SystemTimeZoneCache SYSTEM_ZONE_CACHE = new SystemTimeZoneCache(null);
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

    private static synchronized Set<String> getSystemZIDs() {
        Set<String> set;
        synchronized (ZoneMeta.class) {
            set = null;
            if (REF_SYSTEM_ZONES != null) {
                set = REF_SYSTEM_ZONES.get();
            }
            if (set == null) {
                TreeSet treeSet = new TreeSet();
                String[] zoneIDs = getZoneIDs();
                for (String str : zoneIDs) {
                    if (!str.equals("Etc/Unknown")) {
                        treeSet.add(str);
                    }
                }
                set = Collections.unmodifiableSet(treeSet);
                REF_SYSTEM_ZONES = new SoftReference<>(set);
            }
        }
        return set;
    }

    private static synchronized Set<String> getCanonicalSystemZIDs() {
        Set<String> set;
        synchronized (ZoneMeta.class) {
            set = null;
            if (REF_CANONICAL_SYSTEM_ZONES != null) {
                set = REF_CANONICAL_SYSTEM_ZONES.get();
            }
            if (set == null) {
                TreeSet treeSet = new TreeSet();
                String[] zoneIDs = getZoneIDs();
                for (String str : zoneIDs) {
                    if (!str.equals("Etc/Unknown")) {
                        if (str.equals(getCanonicalCLDRID(str))) {
                            treeSet.add(str);
                        }
                    }
                }
                set = Collections.unmodifiableSet(treeSet);
                REF_CANONICAL_SYSTEM_ZONES = new SoftReference<>(set);
            }
        }
        return set;
    }

    private static synchronized Set<String> getCanonicalSystemLocationZIDs() {
        Set<String> set;
        String region;
        synchronized (ZoneMeta.class) {
            set = null;
            if (REF_CANONICAL_SYSTEM_LOCATION_ZONES != null) {
                set = REF_CANONICAL_SYSTEM_LOCATION_ZONES.get();
            }
            if (set == null) {
                TreeSet treeSet = new TreeSet();
                String[] zoneIDs = getZoneIDs();
                for (String str : zoneIDs) {
                    if (!str.equals("Etc/Unknown")) {
                        if (str.equals(getCanonicalCLDRID(str)) && (region = getRegion(str)) != null && !region.equals(kWorld)) {
                            treeSet.add(str);
                        }
                    }
                }
                set = Collections.unmodifiableSet(treeSet);
                REF_CANONICAL_SYSTEM_LOCATION_ZONES = new SoftReference<>(set);
            }
        }
        return set;
    }

    /* renamed from: ohos.global.icu.impl.ZoneMeta$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$util$TimeZone$SystemTimeZoneType = new int[TimeZone.SystemTimeZoneType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$util$TimeZone$SystemTimeZoneType[TimeZone.SystemTimeZoneType.ANY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$TimeZone$SystemTimeZoneType[TimeZone.SystemTimeZoneType.CANONICAL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$TimeZone$SystemTimeZoneType[TimeZone.SystemTimeZoneType.CANONICAL_LOCATION.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public static Set<String> getAvailableIDs(TimeZone.SystemTimeZoneType systemTimeZoneType, String str, Integer num) {
        Set<String> set;
        OlsonTimeZone systemTimeZone;
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$util$TimeZone$SystemTimeZoneType[systemTimeZoneType.ordinal()];
        if (i == 1) {
            set = getSystemZIDs();
        } else if (i == 2) {
            set = getCanonicalSystemZIDs();
        } else if (i == 3) {
            set = getCanonicalSystemLocationZIDs();
        } else {
            throw new IllegalArgumentException("Unknown SystemTimeZoneType");
        }
        if (str == null && num == null) {
            return set;
        }
        if (str != null) {
            str = str.toUpperCase(Locale.ENGLISH);
        }
        TreeSet treeSet = new TreeSet();
        for (String str2 : set) {
            if ((str == null || str.equals(getRegion(str2))) && (num == null || ((systemTimeZone = getSystemTimeZone(str2)) != null && num.equals(Integer.valueOf(systemTimeZone.getRawOffset()))))) {
                treeSet.add(str2);
            }
        }
        if (treeSet.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(treeSet);
    }

    public static synchronized int countEquivalentIDs(String str) {
        int i;
        synchronized (ZoneMeta.class) {
            i = 0;
            UResourceBundle openOlsonResource = openOlsonResource(null, str);
            if (openOlsonResource != null) {
                try {
                    i = openOlsonResource.get("links").getIntVector().length;
                } catch (MissingResourceException unused) {
                }
            }
        }
        return i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0025, code lost:
        if (r4 != null) goto L_0x0029;
     */
    public static synchronized String getEquivalentID(String str, int i) {
        String str2;
        synchronized (ZoneMeta.class) {
            if (i >= 0) {
                UResourceBundle openOlsonResource = openOlsonResource(null, str);
                if (openOlsonResource != null) {
                    int i2 = -1;
                    try {
                        int[] intVector = openOlsonResource.get("links").getIntVector();
                        if (i < intVector.length) {
                            i2 = intVector[i];
                        }
                    } catch (MissingResourceException unused) {
                    }
                    if (i2 >= 0) {
                        str2 = getZoneID(i2);
                    }
                }
            }
            str2 = "";
        }
        return str2;
    }

    private static synchronized String[] getZoneIDs() {
        String[] strArr;
        synchronized (ZoneMeta.class) {
            if (ZONEIDS == null) {
                try {
                    ZONEIDS = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER).getStringArray(kNAMES);
                } catch (MissingResourceException unused) {
                }
            }
            if (ZONEIDS == null) {
                ZONEIDS = new String[0];
            }
            strArr = ZONEIDS;
        }
        return strArr;
    }

    private static String getZoneID(int i) {
        if (i < 0) {
            return null;
        }
        String[] zoneIDs = getZoneIDs();
        if (i < zoneIDs.length) {
            return zoneIDs[i];
        }
        return null;
    }

    private static int getZoneIndex(String str) {
        String[] zoneIDs = getZoneIDs();
        if (zoneIDs.length > 0) {
            int i = 0;
            int length = zoneIDs.length;
            int i2 = Integer.MAX_VALUE;
            while (true) {
                int i3 = (i + length) / 2;
                if (i2 == i3) {
                    break;
                }
                int compareTo = str.compareTo(zoneIDs[i3]);
                if (compareTo == 0) {
                    return i3;
                }
                if (compareTo < 0) {
                    length = i3;
                } else {
                    i = i3;
                }
                i2 = i3;
            }
        }
        return -1;
    }

    public static String getCanonicalCLDRID(TimeZone timeZone) {
        if (timeZone instanceof OlsonTimeZone) {
            return ((OlsonTimeZone) timeZone).getCanonicalID();
        }
        return getCanonicalCLDRID(timeZone.getID());
    }

    public static String getCanonicalCLDRID(String str) {
        String str2 = CANONICAL_ID_CACHE.get(str);
        if (str2 == null) {
            str2 = findCLDRCanonicalID(str);
            if (str2 == null) {
                try {
                    int zoneIndex = getZoneIndex(str);
                    if (zoneIndex >= 0) {
                        UResourceBundle uResourceBundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER).get(kZONES).get(zoneIndex);
                        if (uResourceBundle.getType() == 7) {
                            str = getZoneID(uResourceBundle.getInt());
                            str2 = findCLDRCanonicalID(str);
                        }
                        if (str2 == null) {
                            str2 = str;
                        }
                    }
                } catch (MissingResourceException unused) {
                }
            }
            if (str2 != null) {
                CANONICAL_ID_CACHE.put(str, str2);
            }
        }
        return str2;
    }

    private static String findCLDRCanonicalID(String str) {
        String replace = str.replace('/', ':');
        String str2 = null;
        try {
            UResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            try {
                bundleInstance.get("typeMap").get("timezone").get(replace);
                str2 = str;
            } catch (MissingResourceException unused) {
            }
            return str2 == null ? bundleInstance.get("typeAlias").get("timezone").getString(replace) : str2;
        } catch (MissingResourceException unused2) {
            return null;
        }
    }

    public static String getRegion(String str) {
        int zoneIndex;
        String str2 = REGION_CACHE.get(str);
        if (str2 == null && (zoneIndex = getZoneIndex(str)) >= 0) {
            try {
                UResourceBundle uResourceBundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER).get(kREGIONS);
                if (zoneIndex < uResourceBundle.getSize()) {
                    str2 = uResourceBundle.getString(zoneIndex);
                }
            } catch (MissingResourceException unused) {
            }
            if (str2 != null) {
                REGION_CACHE.put(str, str2);
            }
        }
        return str2;
    }

    public static String getCanonicalCountry(String str) {
        String region = getRegion(str);
        if (region == null || !region.equals(kWorld)) {
            return region;
        }
        return null;
    }

    public static String getCanonicalCountry(String str, Output<Boolean> output) {
        output.value = Boolean.FALSE;
        String region = getRegion(str);
        if (region != null && region.equals(kWorld)) {
            return null;
        }
        Boolean bool = SINGLE_COUNTRY_CACHE.get(str);
        if (bool == null) {
            boolean z = true;
            if (TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.CANONICAL_LOCATION, region, (Integer) null).size() > 1) {
                z = false;
            }
            bool = Boolean.valueOf(z);
            SINGLE_COUNTRY_CACHE.put(str, bool);
        }
        if (bool.booleanValue()) {
            output.value = Boolean.TRUE;
        } else {
            try {
                String string = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones").get("primaryZones").getString(region);
                if (str.equals(string)) {
                    output.value = Boolean.TRUE;
                } else {
                    String canonicalCLDRID = getCanonicalCLDRID(str);
                    if (canonicalCLDRID != null && canonicalCLDRID.equals(string)) {
                        output.value = Boolean.TRUE;
                    }
                }
            } catch (MissingResourceException unused) {
            }
        }
        return region;
    }

    public static UResourceBundle openOlsonResource(UResourceBundle uResourceBundle, String str) {
        int zoneIndex = getZoneIndex(str);
        if (zoneIndex < 0) {
            return null;
        }
        if (uResourceBundle == null) {
            try {
                uResourceBundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            } catch (MissingResourceException unused) {
                return null;
            }
        }
        UResourceBundle uResourceBundle2 = uResourceBundle.get(kZONES);
        UResourceBundle uResourceBundle3 = uResourceBundle2.get(zoneIndex);
        return uResourceBundle3.getType() == 7 ? uResourceBundle2.get(uResourceBundle3.getInt()) : uResourceBundle3;
    }

    /* access modifiers changed from: private */
    public static class SystemTimeZoneCache extends SoftCache<String, OlsonTimeZone, String> {
        private SystemTimeZoneCache() {
        }

        /* synthetic */ SystemTimeZoneCache(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: protected */
        public OlsonTimeZone createInstance(String str, String str2) {
            try {
                UResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZoneMeta.ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                UResourceBundle openOlsonResource = ZoneMeta.openOlsonResource(bundleInstance, str2);
                if (openOlsonResource == null) {
                    return null;
                }
                OlsonTimeZone olsonTimeZone = new OlsonTimeZone(bundleInstance, openOlsonResource, str2);
                try {
                    olsonTimeZone.freeze();
                } catch (MissingResourceException unused) {
                }
                return olsonTimeZone;
            } catch (MissingResourceException unused2) {
                return null;
            }
        }
    }

    public static OlsonTimeZone getSystemTimeZone(String str) {
        return (OlsonTimeZone) SYSTEM_ZONE_CACHE.getInstance(str, str);
    }

    private static class CustomTimeZoneCache extends SoftCache<Integer, SimpleTimeZone, int[]> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        private CustomTimeZoneCache() {
        }

        /* synthetic */ CustomTimeZoneCache(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: protected */
        public SimpleTimeZone createInstance(Integer num, int[] iArr) {
            SimpleTimeZone simpleTimeZone = new SimpleTimeZone(iArr[0] * ((((iArr[1] * 60) + iArr[2]) * 60) + iArr[3]) * 1000, ZoneMeta.formatCustomID(iArr[1], iArr[2], iArr[3], iArr[0] < 0));
            simpleTimeZone.freeze();
            return simpleTimeZone;
        }
    }

    public static SimpleTimeZone getCustomTimeZone(String str) {
        int[] iArr = new int[4];
        if (!parseCustomID(str, iArr)) {
            return null;
        }
        return (SimpleTimeZone) CUSTOM_ZONE_CACHE.getInstance(Integer.valueOf(iArr[0] * (iArr[1] | (iArr[2] << 5) | (iArr[3] << 11))), iArr);
    }

    public static String getCustomID(String str) {
        int[] iArr = new int[4];
        if (!parseCustomID(str, iArr)) {
            return null;
        }
        boolean z = true;
        int i = iArr[1];
        int i2 = iArr[2];
        int i3 = iArr[3];
        if (iArr[0] >= 0) {
            z = false;
        }
        return formatCustomID(i, i2, i3, z);
    }

    static boolean parseCustomID(String str, int[] iArr) {
        int i;
        int i2;
        int i3;
        int i4;
        if (str != null && str.length() > 3 && str.toUpperCase(Locale.ENGLISH).startsWith("GMT")) {
            ParsePosition parsePosition = new ParsePosition(3);
            if (str.charAt(parsePosition.getIndex()) == '-') {
                i = -1;
            } else if (str.charAt(parsePosition.getIndex()) != '+') {
                return false;
            } else {
                i = 1;
            }
            parsePosition.setIndex(parsePosition.getIndex() + 1);
            NumberFormat instance = NumberFormat.getInstance();
            instance.setParseIntegerOnly(true);
            int index = parsePosition.getIndex();
            Number parse = instance.parse(str, parsePosition);
            if (parsePosition.getIndex() == index) {
                return false;
            }
            int intValue = parse.intValue();
            if (parsePosition.getIndex() >= str.length()) {
                int index2 = parsePosition.getIndex() - index;
                if (index2 > 0 && 6 >= index2) {
                    switch (index2) {
                        case 1:
                        case 2:
                        default:
                            i2 = 0;
                            i3 = 0;
                            break;
                        case 3:
                        case 4:
                            i2 = intValue % 100;
                            intValue /= 100;
                            i3 = 0;
                            break;
                        case 5:
                        case 6:
                            int i5 = intValue % 100;
                            int i6 = (intValue / 100) % 100;
                            intValue /= 10000;
                            i3 = i5;
                            i2 = i6;
                            break;
                    }
                }
            } else if (parsePosition.getIndex() - index > 2 || str.charAt(parsePosition.getIndex()) != ':') {
                return false;
            } else {
                parsePosition.setIndex(parsePosition.getIndex() + 1);
                int index3 = parsePosition.getIndex();
                Number parse2 = instance.parse(str, parsePosition);
                if (parsePosition.getIndex() - index3 != 2) {
                    return false;
                }
                int intValue2 = parse2.intValue();
                if (parsePosition.getIndex() >= str.length()) {
                    i4 = 0;
                } else if (str.charAt(parsePosition.getIndex()) != ':') {
                    return false;
                } else {
                    parsePosition.setIndex(parsePosition.getIndex() + 1);
                    int index4 = parsePosition.getIndex();
                    Number parse3 = instance.parse(str, parsePosition);
                    if (parsePosition.getIndex() != str.length() || parsePosition.getIndex() - index4 != 2) {
                        return false;
                    }
                    i4 = parse3.intValue();
                }
                i3 = i4;
                i2 = intValue2;
            }
            if (intValue <= 23 && i2 <= 59 && i3 <= 59) {
                if (iArr != null) {
                    if (iArr.length >= 1) {
                        iArr[0] = i;
                    }
                    if (iArr.length >= 2) {
                        iArr[1] = intValue;
                    }
                    if (iArr.length >= 3) {
                        iArr[2] = i2;
                    }
                    if (iArr.length >= 4) {
                        iArr[3] = i3;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static SimpleTimeZone getCustomTimeZone(int i) {
        int i2;
        boolean z;
        if (i < 0) {
            z = true;
            i2 = -i;
        } else {
            z = false;
            i2 = i;
        }
        int i3 = i2 / 1000;
        int i4 = i3 % 60;
        int i5 = i3 / 60;
        return new SimpleTimeZone(i, formatCustomID(i5 / 60, i5 % 60, i4, z));
    }

    static String formatCustomID(int i, int i2, int i3, boolean z) {
        StringBuilder sb = new StringBuilder("GMT");
        if (!(i == 0 && i2 == 0)) {
            if (z) {
                sb.append(LocaleUtility.IETF_SEPARATOR);
            } else {
                sb.append('+');
            }
            if (i < 10) {
                sb.append('0');
            }
            sb.append(i);
            sb.append(':');
            if (i2 < 10) {
                sb.append('0');
            }
            sb.append(i2);
            if (i3 != 0) {
                sb.append(':');
                if (i3 < 10) {
                    sb.append('0');
                }
                sb.append(i3);
            }
        }
        return sb.toString();
    }

    public static String getShortID(TimeZone timeZone) {
        String str;
        if (timeZone instanceof OlsonTimeZone) {
            str = ((OlsonTimeZone) timeZone).getCanonicalID();
        } else {
            str = getCanonicalCLDRID(timeZone.getID());
        }
        if (str == null) {
            return null;
        }
        return getShortIDFromCanonical(str);
    }

    public static String getShortID(String str) {
        String canonicalCLDRID = getCanonicalCLDRID(str);
        if (canonicalCLDRID == null) {
            return null;
        }
        return getShortIDFromCanonical(canonicalCLDRID);
    }

    private static String getShortIDFromCanonical(String str) {
        try {
            return UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("typeMap").get("timezone").getString(str.replace('/', ':'));
        } catch (MissingResourceException unused) {
            return null;
        }
    }
}
