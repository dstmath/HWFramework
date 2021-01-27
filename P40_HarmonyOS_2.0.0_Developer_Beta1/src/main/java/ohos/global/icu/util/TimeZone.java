package ohos.global.icu.util;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Logger;
import ohos.global.icu.impl.Grego;
import ohos.global.icu.impl.ICUConfig;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.JavaTimeZone;
import ohos.global.icu.impl.OlsonTimeZone;
import ohos.global.icu.impl.TimeZoneAdapter;
import ohos.global.icu.impl.ZoneMeta;
import ohos.global.icu.text.TimeZoneFormat;
import ohos.global.icu.text.TimeZoneNames;
import ohos.global.icu.util.ULocale;

public abstract class TimeZone implements Serializable, Cloneable, Freezable<TimeZone> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int GENERIC_LOCATION = 7;
    public static final TimeZone GMT_ZONE = new ConstantZone(0, GMT_ZONE_ID).freeze();
    static final String GMT_ZONE_ID = "Etc/GMT";
    private static final Logger LOGGER = Logger.getLogger("ohos.global.icu.util.TimeZone");
    public static final int LONG = 1;
    public static final int LONG_GENERIC = 3;
    public static final int LONG_GMT = 5;
    public static final int SHORT = 0;
    public static final int SHORT_COMMONLY_USED = 6;
    public static final int SHORT_GENERIC = 2;
    public static final int SHORT_GMT = 4;
    public static final int TIMEZONE_ICU = 0;
    public static final int TIMEZONE_JDK = 1;
    private static final String TZIMPL_CONFIG_ICU = "ICU";
    private static final String TZIMPL_CONFIG_JDK = "JDK";
    private static final String TZIMPL_CONFIG_KEY = "ohos.global.icu.util.TimeZone.DefaultTimeZoneType";
    private static int TZ_IMPL = 0;
    public static final TimeZone UNKNOWN_ZONE = new ConstantZone(0, UNKNOWN_ZONE_ID).freeze();
    public static final String UNKNOWN_ZONE_ID = "Etc/Unknown";
    private static volatile TimeZone defaultZone = null;
    private static final long serialVersionUID = -744942128318337471L;
    private String ID;

    public enum SystemTimeZoneType {
        ANY,
        CANONICAL,
        CANONICAL_LOCATION
    }

    public abstract int getOffset(int i, int i2, int i3, int i4, int i5, int i6);

    public abstract int getRawOffset();

    public abstract boolean inDaylightTime(Date date);

    @Override // ohos.global.icu.util.Freezable
    public boolean isFrozen() {
        return false;
    }

    public abstract void setRawOffset(int i);

    public abstract boolean useDaylightTime();

    static {
        TZ_IMPL = 0;
        if (ICUConfig.get(TZIMPL_CONFIG_KEY, TZIMPL_CONFIG_ICU).equalsIgnoreCase(TZIMPL_CONFIG_JDK)) {
            TZ_IMPL = 1;
        }
    }

    public TimeZone() {
    }

    @Deprecated
    protected TimeZone(String str) {
        if (str != null) {
            this.ID = str;
            return;
        }
        throw new NullPointerException();
    }

    public int getOffset(long j) {
        int[] iArr = new int[2];
        getOffset(j, false, iArr);
        return iArr[0] + iArr[1];
    }

    public void getOffset(long j, boolean z, int[] iArr) {
        iArr[0] = getRawOffset();
        if (!z) {
            j += (long) iArr[0];
        }
        int[] iArr2 = new int[6];
        int i = 0;
        while (true) {
            Grego.timeToFields(j, iArr2);
            iArr[1] = getOffset(1, iArr2[0], iArr2[1], iArr2[2], iArr2[3], iArr2[5]) - iArr[0];
            if (i == 0 && z && iArr[1] != 0) {
                j -= (long) iArr[1];
                i++;
            } else {
                return;
            }
        }
    }

    public String getID() {
        return this.ID;
    }

    public void setID(String str) {
        if (str == null) {
            throw new NullPointerException();
        } else if (!isFrozen()) {
            this.ID = str;
        } else {
            throw new UnsupportedOperationException("Attempt to modify a frozen TimeZone instance.");
        }
    }

    public final String getDisplayName() {
        return _getDisplayName(3, false, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public final String getDisplayName(Locale locale) {
        return _getDisplayName(3, false, ULocale.forLocale(locale));
    }

    public final String getDisplayName(ULocale uLocale) {
        return _getDisplayName(3, false, uLocale);
    }

    public final String getDisplayName(boolean z, int i) {
        return getDisplayName(z, i, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String getDisplayName(boolean z, int i, Locale locale) {
        return getDisplayName(z, i, ULocale.forLocale(locale));
    }

    public String getDisplayName(boolean z, int i, ULocale uLocale) {
        if (i >= 0 && i <= 7) {
            return _getDisplayName(i, z, uLocale);
        }
        throw new IllegalArgumentException("Illegal style: " + i);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
        if (r12 != 6) goto L_0x0035;
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:63:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private String _getDisplayName(int i, boolean z, ULocale uLocale) {
        String formatOffsetLocalizedGMT;
        String displayName;
        if (uLocale != null) {
            String str = null;
            TimeZoneNames.NameType nameType = null;
            str = null;
            if (i == 7 || i == 3 || i == 2) {
                TimeZoneFormat instance = TimeZoneFormat.getInstance(uLocale);
                long currentTimeMillis = System.currentTimeMillis();
                Output<TimeZoneFormat.TimeType> output = new Output<>(TimeZoneFormat.TimeType.UNKNOWN);
                if (i == 2) {
                    str = instance.format(TimeZoneFormat.Style.GENERIC_SHORT, this, currentTimeMillis, output);
                } else if (i == 3) {
                    str = instance.format(TimeZoneFormat.Style.GENERIC_LONG, this, currentTimeMillis, output);
                } else if (i == 7) {
                    str = instance.format(TimeZoneFormat.Style.GENERIC_LOCATION, this, currentTimeMillis, output);
                }
                if ((z && output.value == TimeZoneFormat.TimeType.STANDARD) || (!z && output.value == TimeZoneFormat.TimeType.DAYLIGHT)) {
                    int rawOffset = z ? getRawOffset() + getDSTSavings() : getRawOffset();
                    if (i == 2) {
                        formatOffsetLocalizedGMT = instance.formatOffsetShortLocalizedGMT(rawOffset);
                    } else {
                        formatOffsetLocalizedGMT = instance.formatOffsetLocalizedGMT(rawOffset);
                    }
                }
                return str;
            } else if (i == 5 || i == 4) {
                TimeZoneFormat instance2 = TimeZoneFormat.getInstance(uLocale);
                int rawOffset2 = (!z || !useDaylightTime()) ? getRawOffset() : getRawOffset() + getDSTSavings();
                if (i == 4) {
                    str = instance2.formatOffsetISO8601Basic(rawOffset2, false, false, false);
                } else if (i == 5) {
                    str = instance2.formatOffsetLocalizedGMT(rawOffset2);
                }
                return str;
            } else {
                long currentTimeMillis2 = System.currentTimeMillis();
                TimeZoneNames instance3 = TimeZoneNames.getInstance(uLocale);
                if (i != 0) {
                    if (i == 1) {
                        nameType = z ? TimeZoneNames.NameType.LONG_DAYLIGHT : TimeZoneNames.NameType.LONG_STANDARD;
                    }
                    displayName = instance3.getDisplayName(ZoneMeta.getCanonicalCLDRID(this), nameType, currentTimeMillis2);
                    if (displayName == null) {
                        return displayName;
                    }
                    TimeZoneFormat instance4 = TimeZoneFormat.getInstance(uLocale);
                    int rawOffset3 = (!z || !useDaylightTime()) ? getRawOffset() : getRawOffset() + getDSTSavings();
                    if (i == 1) {
                        formatOffsetLocalizedGMT = instance4.formatOffsetLocalizedGMT(rawOffset3);
                    } else {
                        formatOffsetLocalizedGMT = instance4.formatOffsetShortLocalizedGMT(rawOffset3);
                    }
                }
                nameType = z ? TimeZoneNames.NameType.SHORT_DAYLIGHT : TimeZoneNames.NameType.SHORT_STANDARD;
                displayName = instance3.getDisplayName(ZoneMeta.getCanonicalCLDRID(this), nameType, currentTimeMillis2);
                if (displayName == null) {
                }
            }
            return formatOffsetLocalizedGMT;
        }
        throw new NullPointerException("locale is null");
    }

    public int getDSTSavings() {
        return useDaylightTime() ? 3600000 : 0;
    }

    public boolean observesDaylightTime() {
        return useDaylightTime() || inDaylightTime(new Date());
    }

    public static TimeZone getTimeZone(String str) {
        return getTimeZone(str, TZ_IMPL, false);
    }

    public static TimeZone getFrozenTimeZone(String str) {
        return getTimeZone(str, TZ_IMPL, true);
    }

    public static TimeZone getTimeZone(String str, int i) {
        return getTimeZone(str, i, false);
    }

    private static TimeZone getTimeZone(String str, int i, boolean z) {
        TimeZone timeZone;
        if (i == 1) {
            JavaTimeZone createTimeZone = JavaTimeZone.createTimeZone(str);
            if (createTimeZone != null) {
                return z ? createTimeZone.freeze() : createTimeZone;
            }
            timeZone = getFrozenICUTimeZone(str, false);
        } else {
            timeZone = getFrozenICUTimeZone(str, true);
        }
        if (timeZone == null) {
            Logger logger = LOGGER;
            logger.fine("\"" + str + "\" is a bogus id so timezone is falling back to Etc/Unknown(GMT).");
            timeZone = UNKNOWN_ZONE;
        }
        return z ? timeZone : timeZone.cloneAsThawed();
    }

    static BasicTimeZone getFrozenICUTimeZone(String str, boolean z) {
        OlsonTimeZone systemTimeZone = z ? ZoneMeta.getSystemTimeZone(str) : null;
        return systemTimeZone == null ? ZoneMeta.getCustomTimeZone(str) : systemTimeZone;
    }

    public static synchronized void setDefaultTimeZoneType(int i) {
        synchronized (TimeZone.class) {
            if (i == 0 || i == 1) {
                TZ_IMPL = i;
            } else {
                throw new IllegalArgumentException("Invalid timezone type");
            }
        }
    }

    public static int getDefaultTimeZoneType() {
        return TZ_IMPL;
    }

    public static Set<String> getAvailableIDs(SystemTimeZoneType systemTimeZoneType, String str, Integer num) {
        return ZoneMeta.getAvailableIDs(systemTimeZoneType, str, num);
    }

    public static String[] getAvailableIDs(int i) {
        return (String[]) getAvailableIDs(SystemTimeZoneType.ANY, null, Integer.valueOf(i)).toArray(new String[0]);
    }

    public static String[] getAvailableIDs(String str) {
        return (String[]) getAvailableIDs(SystemTimeZoneType.ANY, str, null).toArray(new String[0]);
    }

    public static String[] getAvailableIDs() {
        return (String[]) getAvailableIDs(SystemTimeZoneType.ANY, null, null).toArray(new String[0]);
    }

    public static int countEquivalentIDs(String str) {
        return ZoneMeta.countEquivalentIDs(str);
    }

    public static String getEquivalentID(String str, int i) {
        return ZoneMeta.getEquivalentID(str, i);
    }

    public static TimeZone getDefault() {
        JavaTimeZone javaTimeZone;
        JavaTimeZone javaTimeZone2 = defaultZone;
        if (javaTimeZone2 == null) {
            synchronized (java.util.TimeZone.class) {
                synchronized (TimeZone.class) {
                    javaTimeZone = defaultZone;
                    if (javaTimeZone == null) {
                        if (TZ_IMPL == 1) {
                            javaTimeZone = new JavaTimeZone();
                        } else {
                            javaTimeZone = getFrozenTimeZone(java.util.TimeZone.getDefault().getID());
                        }
                        defaultZone = javaTimeZone;
                    }
                }
            }
            javaTimeZone2 = javaTimeZone;
        }
        return javaTimeZone2.cloneAsThawed();
    }

    public static synchronized void setDefault(TimeZone timeZone) {
        synchronized (TimeZone.class) {
            setICUDefault(timeZone);
            if (timeZone != null) {
                java.util.TimeZone timeZone2 = null;
                if (timeZone instanceof JavaTimeZone) {
                    timeZone2 = ((JavaTimeZone) timeZone).unwrap();
                } else if (timeZone instanceof OlsonTimeZone) {
                    String id = timeZone.getID();
                    java.util.TimeZone timeZone3 = java.util.TimeZone.getTimeZone(id);
                    if (!id.equals(timeZone3.getID())) {
                        String canonicalID = getCanonicalID(id);
                        timeZone3 = java.util.TimeZone.getTimeZone(canonicalID);
                        if (!canonicalID.equals(timeZone3.getID())) {
                        }
                    }
                    timeZone2 = timeZone3;
                }
                if (timeZone2 == null) {
                    timeZone2 = TimeZoneAdapter.wrap(timeZone);
                }
                java.util.TimeZone.setDefault(timeZone2);
            }
        }
    }

    @Deprecated
    public static synchronized void setICUDefault(TimeZone timeZone) {
        synchronized (TimeZone.class) {
            if (timeZone == null) {
                defaultZone = null;
            } else if (timeZone.isFrozen()) {
                defaultZone = timeZone;
            } else {
                defaultZone = ((TimeZone) timeZone.clone()).freeze();
            }
        }
    }

    public boolean hasSameRules(TimeZone timeZone) {
        return timeZone != null && getRawOffset() == timeZone.getRawOffset() && useDaylightTime() == timeZone.useDaylightTime();
    }

    @Override // java.lang.Object
    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.ID.equals(((TimeZone) obj).ID);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.ID.hashCode();
    }

    public static String getTZDataVersion() {
        return VersionInfo.getTZDataVersion();
    }

    public static String getCanonicalID(String str) {
        return getCanonicalID(str, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0023  */
    public static String getCanonicalID(String str, boolean[] zArr) {
        boolean z;
        Object obj = UNKNOWN_ZONE_ID;
        if (str == null || str.length() == 0) {
            obj = null;
        } else if (!str.equals(obj)) {
            obj = ZoneMeta.getCanonicalCLDRID(str);
            if (obj != null) {
                z = true;
                if (zArr != null) {
                    zArr[0] = z;
                }
                return obj;
            }
            obj = ZoneMeta.getCustomID(str);
        }
        z = false;
        if (zArr != null) {
        }
        return obj;
    }

    public static String getRegion(String str) {
        String region = !str.equals(UNKNOWN_ZONE_ID) ? ZoneMeta.getRegion(str) : null;
        if (region != null) {
            return region;
        }
        throw new IllegalArgumentException("Unknown system zone id: " + str);
    }

    public static String getWindowsID(String str) {
        boolean[] zArr = {false};
        String canonicalID = getCanonicalID(str, zArr);
        if (!zArr[0]) {
            return null;
        }
        UResourceBundleIterator iterator = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "windowsZones", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("mapTimezones").getIterator();
        while (iterator.hasNext()) {
            UResourceBundle next = iterator.next();
            if (next.getType() == 2) {
                UResourceBundleIterator iterator2 = next.getIterator();
                while (iterator2.hasNext()) {
                    UResourceBundle next2 = iterator2.next();
                    if (next2.getType() == 0) {
                        for (String str2 : next2.getString().split(" ")) {
                            if (str2.equals(canonicalID)) {
                                return next.getKey();
                            }
                        }
                        continue;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static String getIDForWindowsID(String str, String str2) {
        String str3 = null;
        try {
            UResourceBundle uResourceBundle = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "windowsZones", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("mapTimezones").get(str);
            if (str2 != null) {
                try {
                    String string = uResourceBundle.getString(str2);
                    if (string != null) {
                        try {
                            int indexOf = string.indexOf(32);
                            if (indexOf > 0) {
                                string = string.substring(0, indexOf);
                            }
                        } catch (MissingResourceException unused) {
                        }
                    }
                    str3 = string;
                } catch (MissingResourceException unused2) {
                }
            }
            return str3 == null ? uResourceBundle.getString("001") : str3;
        } catch (MissingResourceException unused3) {
            return null;
        }
    }

    @Override // ohos.global.icu.util.Freezable
    public TimeZone freeze() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    @Override // ohos.global.icu.util.Freezable
    public TimeZone cloneAsThawed() {
        try {
            return (TimeZone) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    private static final class ConstantZone extends TimeZone {
        private static final long serialVersionUID = 1;
        private volatile transient boolean isFrozen;
        private int rawOffset;

        @Override // ohos.global.icu.util.TimeZone
        public boolean inDaylightTime(Date date) {
            return false;
        }

        @Override // ohos.global.icu.util.TimeZone
        public boolean useDaylightTime() {
            return false;
        }

        private ConstantZone(int i, String str) {
            super(str);
            this.isFrozen = false;
            this.rawOffset = i;
        }

        @Override // ohos.global.icu.util.TimeZone
        public int getOffset(int i, int i2, int i3, int i4, int i5, int i6) {
            return this.rawOffset;
        }

        @Override // ohos.global.icu.util.TimeZone
        public void setRawOffset(int i) {
            if (!isFrozen()) {
                this.rawOffset = i;
                return;
            }
            throw new UnsupportedOperationException("Attempt to modify a frozen TimeZone instance.");
        }

        @Override // ohos.global.icu.util.TimeZone
        public int getRawOffset() {
            return this.rawOffset;
        }

        @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
        public boolean isFrozen() {
            return this.isFrozen;
        }

        @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
        public TimeZone freeze() {
            this.isFrozen = true;
            return this;
        }

        @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
        public TimeZone cloneAsThawed() {
            ConstantZone constantZone = (ConstantZone) TimeZone.super.cloneAsThawed();
            constantZone.isFrozen = false;
            return constantZone;
        }
    }
}
