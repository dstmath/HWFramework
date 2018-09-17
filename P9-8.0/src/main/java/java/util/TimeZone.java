package java.util;

import java.io.IOException;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Locale.Category;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.icu.TimeZoneNames;
import libcore.io.IoUtils;
import libcore.util.ZoneInfoDB;
import org.apache.harmony.luni.internal.util.TimezoneGetter;

public abstract class TimeZone implements Serializable, Cloneable {
    private static final TimeZone GMT = new SimpleTimeZone(0, "GMT");
    public static final int LONG = 1;
    static final TimeZone NO_TIMEZONE = null;
    public static final int SHORT = 0;
    private static final TimeZone UTC = new SimpleTimeZone(0, "UTC");
    private static volatile TimeZone defaultTimeZone = null;
    static final long serialVersionUID = 3581463369166924961L;
    private String ID;

    private static class NoImagePreloadHolder {
        public static final Pattern CUSTOM_ZONE_ID_PATTERN = Pattern.compile("^GMT[-+](\\d{1,2})(:?(\\d\\d))?$");

        private NoImagePreloadHolder() {
        }
    }

    private static native String getSystemGMTOffsetID();

    private static native String getSystemTimeZoneID(String str, String str2);

    public abstract int getOffset(int i, int i2, int i3, int i4, int i5, int i6);

    public abstract int getRawOffset();

    public abstract boolean inDaylightTime(Date date);

    public abstract void setRawOffset(int i);

    public abstract boolean useDaylightTime();

    public int getOffset(long date) {
        if (inDaylightTime(new Date(date))) {
            return getRawOffset() + getDSTSavings();
        }
        return getRawOffset();
    }

    int getOffsets(long date, int[] offsets) {
        int rawoffset = getRawOffset();
        int dstoffset = 0;
        if (inDaylightTime(new Date(date))) {
            dstoffset = getDSTSavings();
        }
        if (offsets != null) {
            offsets[0] = rawoffset;
            offsets[1] = dstoffset;
        }
        return rawoffset + dstoffset;
    }

    public String getID() {
        return this.ID;
    }

    public void setID(String ID) {
        if (ID == null) {
            throw new NullPointerException();
        }
        this.ID = ID;
    }

    public final String getDisplayName() {
        return getDisplayName(false, 1, Locale.getDefault(Category.DISPLAY));
    }

    public final String getDisplayName(Locale locale) {
        return getDisplayName(false, 1, locale);
    }

    public final String getDisplayName(boolean daylight, int style) {
        return getDisplayName(daylight, style, Locale.getDefault(Category.DISPLAY));
    }

    public String getDisplayName(boolean daylightTime, int style, Locale locale) {
        if (style == 0 || style == 1) {
            String result = TimeZoneNames.getDisplayName(TimeZoneNames.getZoneStrings(locale), getID(), daylightTime, style);
            if (result != null) {
                return result;
            }
            int offsetMillis = getRawOffset();
            if (daylightTime) {
                offsetMillis += getDSTSavings();
            }
            return createGmtOffsetString(true, true, offsetMillis);
        }
        throw new IllegalArgumentException("Illegal style: " + style);
    }

    public static String createGmtOffsetString(boolean includeGmt, boolean includeMinuteSeparator, int offsetMillis) {
        int offsetMinutes = offsetMillis / 60000;
        char sign = '+';
        if (offsetMinutes < 0) {
            sign = '-';
            offsetMinutes = -offsetMinutes;
        }
        StringBuilder builder = new StringBuilder(9);
        if (includeGmt) {
            builder.append("GMT");
        }
        builder.append(sign);
        appendNumber(builder, 2, offsetMinutes / 60);
        if (includeMinuteSeparator) {
            builder.append(':');
        }
        appendNumber(builder, 2, offsetMinutes % 60);
        return builder.toString();
    }

    private static void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
    }

    public int getDSTSavings() {
        if (useDaylightTime()) {
            return 3600000;
        }
        return 0;
    }

    public boolean observesDaylightTime() {
        return !useDaylightTime() ? inDaylightTime(new Date()) : true;
    }

    /* JADX WARNING: Missing block: B:35:0x005f, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized TimeZone getTimeZone(String id) {
        synchronized (TimeZone.class) {
            if (id == null) {
                throw new NullPointerException("id == null");
            }
            if (id.length() == 3) {
                TimeZone timeZone;
                if (id.equals("GMT")) {
                    timeZone = (TimeZone) GMT.clone();
                    return timeZone;
                } else if (id.equals("UTC")) {
                    timeZone = (TimeZone) UTC.clone();
                    return timeZone;
                }
            }
            TimeZone zone = null;
            try {
                zone = ZoneInfoDB.getInstance().makeTimeZone(id);
            } catch (IOException e) {
            }
            if (zone == null) {
                if (id.length() > 3 && id.startsWith("GMT")) {
                    zone = getCustomTimeZone(id);
                }
            }
            if (zone == null) {
                zone = (TimeZone) GMT.clone();
            }
        }
    }

    public static TimeZone getTimeZone(ZoneId zoneId) {
        String tzid = zoneId.getId();
        char c = tzid.charAt(0);
        if (c == '+' || c == '-') {
            tzid = "GMT" + tzid;
        } else if (c == 'Z' && tzid.length() == 1) {
            tzid = "UTC";
        }
        return getTimeZone(tzid);
    }

    public ZoneId toZoneId() {
        return ZoneId.of(getID(), ZoneId.SHORT_IDS);
    }

    private static TimeZone getCustomTimeZone(String id) {
        Matcher m = NoImagePreloadHolder.CUSTOM_ZONE_ID_PATTERN.matcher(id);
        if (!m.matches()) {
            return null;
        }
        int minute = 0;
        try {
            int hour = Integer.parseInt(m.group(1));
            if (m.group(3) != null) {
                minute = Integer.parseInt(m.group(3));
            }
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }
            int raw = (3600000 * hour) + (60000 * minute);
            if (id.charAt(3) == '-') {
                raw = -raw;
            }
            return new SimpleTimeZone(raw, String.format(Locale.ROOT, "GMT%c%02d:%02d", Character.valueOf(id.charAt(3)), Integer.valueOf(hour), Integer.valueOf(minute)));
        } catch (Object impossible) {
            throw new AssertionError(impossible);
        }
    }

    public static synchronized String[] getAvailableIDs(int rawOffset) {
        String[] availableIDs;
        synchronized (TimeZone.class) {
            availableIDs = ZoneInfoDB.getInstance().getAvailableIDs(rawOffset);
        }
        return availableIDs;
    }

    public static synchronized String[] getAvailableIDs() {
        String[] availableIDs;
        synchronized (TimeZone.class) {
            availableIDs = ZoneInfoDB.getInstance().getAvailableIDs();
        }
        return availableIDs;
    }

    public static TimeZone getDefault() {
        return (TimeZone) getDefaultRef().clone();
    }

    static synchronized TimeZone getDefaultRef() {
        TimeZone timeZone;
        synchronized (TimeZone.class) {
            if (defaultTimeZone == null) {
                TimezoneGetter tzGetter = TimezoneGetter.getInstance();
                String zoneName = tzGetter != null ? tzGetter.getId() : null;
                if (zoneName != null) {
                    zoneName = zoneName.trim();
                }
                if (zoneName == null || zoneName.isEmpty()) {
                    try {
                        zoneName = IoUtils.readFileAsString("/etc/timezone");
                    } catch (IOException e) {
                        zoneName = "GMT";
                    }
                }
                defaultTimeZone = getTimeZone(zoneName);
            }
            timeZone = defaultTimeZone;
        }
        return timeZone;
    }

    public static synchronized void setDefault(TimeZone timeZone) {
        TimeZone timeZone2 = null;
        synchronized (TimeZone.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new PropertyPermission("user.timezone", "write"));
            }
            if (timeZone != null) {
                timeZone2 = (TimeZone) timeZone.clone();
            }
            defaultTimeZone = timeZone2;
            android.icu.util.TimeZone.clearCachedDefault();
        }
    }

    public boolean hasSameRules(TimeZone other) {
        if (other != null && getRawOffset() == other.getRawOffset() && useDaylightTime() == other.useDaylightTime()) {
            return true;
        }
        return false;
    }

    public Object clone() {
        try {
            TimeZone other = (TimeZone) super.clone();
            other.ID = this.ID;
            return other;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }
}
