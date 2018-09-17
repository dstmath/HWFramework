package java.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale.Category;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.icu.TimeZoneNames;
import libcore.io.IoUtils;
import libcore.util.ZoneInfoDB;
import org.apache.harmony.luni.internal.util.TimezoneGetter;
import sun.security.util.SecurityConstants;

public abstract class TimeZone implements Serializable, Cloneable {
    private static final TimeZone GMT = null;
    public static final int LONG = 1;
    static final TimeZone NO_TIMEZONE = null;
    public static final int SHORT = 0;
    private static final TimeZone UTC = null;
    private static volatile TimeZone defaultTimeZone = null;
    static final long serialVersionUID = 3581463369166924961L;
    private String ID;

    private static class NoImagePreloadHolder {
        public static final Pattern CUSTOM_ZONE_ID_PATTERN = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.TimeZone.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.TimeZone.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.TimeZone.NoImagePreloadHolder.<clinit>():void");
        }

        private NoImagePreloadHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.TimeZone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.TimeZone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.TimeZone.<clinit>():void");
    }

    private static native String getSystemGMTOffsetID();

    private static native String getSystemTimeZoneID(String str, String str2);

    public abstract int getOffset(int i, int i2, int i3, int i4, int i5, int i6);

    public abstract int getRawOffset();

    public abstract boolean inDaylightTime(Date date);

    public abstract void setRawOffset(int i);

    public abstract boolean useDaylightTime();

    public TimeZone() {
    }

    public int getOffset(long date) {
        if (inDaylightTime(new Date(date))) {
            return getRawOffset() + getDSTSavings();
        }
        return getRawOffset();
    }

    int getOffsets(long date, int[] offsets) {
        int rawoffset = getRawOffset();
        int dstoffset = SHORT;
        if (inDaylightTime(new Date(date))) {
            dstoffset = getDSTSavings();
        }
        if (offsets != null) {
            offsets[SHORT] = rawoffset;
            offsets[LONG] = dstoffset;
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
        return getDisplayName(false, LONG, Locale.getDefault(Category.DISPLAY));
    }

    public final String getDisplayName(Locale locale) {
        return getDisplayName(false, LONG, locale);
    }

    public final String getDisplayName(boolean daylight, int style) {
        return getDisplayName(daylight, style, Locale.getDefault(Category.DISPLAY));
    }

    public String getDisplayName(boolean daylightTime, int style, Locale locale) {
        if (style == 0 || style == LONG) {
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
        throw new IllegalArgumentException("Bad style: " + style);
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
        for (int i = SHORT; i < count - string.length(); i += LONG) {
            builder.append('0');
        }
        builder.append(string);
    }

    public int getDSTSavings() {
        if (useDaylightTime()) {
            return 3600000;
        }
        return SHORT;
    }

    public boolean observesDaylightTime() {
        return !useDaylightTime() ? inDaylightTime(new Date()) : true;
    }

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
            return zone;
        }
    }

    private static TimeZone getCustomTimeZone(String id) {
        Matcher m = NoImagePreloadHolder.CUSTOM_ZONE_ID_PATTERN.matcher(id);
        if (!m.matches()) {
            return null;
        }
        int minute = SHORT;
        try {
            int hour = Integer.parseInt(m.group((int) LONG));
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
                String id = tzGetter != null ? tzGetter.getId() : null;
                if (id != null) {
                    id = id.trim();
                }
                if (id == null || id.isEmpty()) {
                    try {
                        id = IoUtils.readFileAsString("/etc/timezone");
                    } catch (IOException e) {
                        id = "GMT";
                    }
                }
                defaultTimeZone = getTimeZone(id);
            }
            timeZone = defaultTimeZone;
        }
        return timeZone;
    }

    private static boolean hasPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return true;
        }
        try {
            sm.checkPermission(new PropertyPermission("user.timezone", SecurityConstants.PROPERTY_WRITE_ACTION));
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static synchronized void setDefault(TimeZone timeZone) {
        TimeZone timeZone2 = null;
        synchronized (TimeZone.class) {
            if (hasPermission()) {
                if (timeZone != null) {
                    timeZone2 = (TimeZone) timeZone.clone();
                }
                defaultTimeZone = timeZone2;
                android.icu.util.TimeZone.clearCachedDefault();
            }
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
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
