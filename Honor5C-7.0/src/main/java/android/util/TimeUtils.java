package android.util;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.text.format.DateFormat;
import com.android.internal.R;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import libcore.util.ZoneInfoDB;
import org.xmlpull.v1.XmlPullParserException;

public class TimeUtils {
    private static final boolean DBG = false;
    public static final int HUNDRED_DAY_FIELD_LEN = 19;
    public static final long NANOS_PER_MS = 1000000;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final String TAG = "TimeUtils";
    private static char[] sFormatStr;
    private static final Object sFormatSync = null;
    private static String sLastCountry;
    private static final Object sLastLockObj = null;
    private static String sLastUniqueCountry;
    private static final Object sLastUniqueLockObj = null;
    private static ArrayList<TimeZone> sLastUniqueZoneOffsets;
    private static ArrayList<TimeZone> sLastZones;
    private static SimpleDateFormat sLoggingFormat;
    private static char[] sTmpFormatStr;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.TimeUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.TimeUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.TimeUtils.<clinit>():void");
    }

    public static TimeZone getTimeZone(int offset, boolean dst, long when, String country) {
        TimeZone best = null;
        Date d = new Date(when);
        TimeZone current = TimeZone.getDefault();
        String currentName = current.getID();
        int currentOffset = current.getOffset(when);
        boolean currentDst = current.inDaylightTime(d);
        for (TimeZone tz : getTimeZones(country)) {
            if (tz.getID().equals(currentName) && currentOffset == offset && currentDst == dst) {
                return current;
            }
            if (best == null && tz.getOffset(when) == offset && tz.inDaylightTime(d) == dst) {
                best = tz;
            }
        }
        return best;
    }

    public static ArrayList<TimeZone> getTimeZonesWithUniqueOffsets(String country) {
        synchronized (sLastUniqueLockObj) {
            ArrayList<TimeZone> arrayList;
            if (country != null) {
                if (country.equals(sLastUniqueCountry)) {
                    arrayList = sLastUniqueZoneOffsets;
                    return arrayList;
                }
            }
            Collection<TimeZone> zones = getTimeZones(country);
            ArrayList<TimeZone> uniqueTimeZones = new ArrayList();
            for (TimeZone zone : zones) {
                boolean found = DBG;
                for (int i = 0; i < uniqueTimeZones.size(); i++) {
                    if (((TimeZone) uniqueTimeZones.get(i)).getRawOffset() == zone.getRawOffset()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    uniqueTimeZones.add(zone);
                }
            }
            synchronized (sLastUniqueLockObj) {
                sLastUniqueZoneOffsets = uniqueTimeZones;
                sLastUniqueCountry = country;
                arrayList = sLastUniqueZoneOffsets;
            }
            return arrayList;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<TimeZone> getTimeZones(String country) {
        synchronized (sLastLockObj) {
            if (country != null) {
                if (country.equals(sLastCountry)) {
                    ArrayList<TimeZone> arrayList = sLastZones;
                    return arrayList;
                }
            }
            ArrayList<TimeZone> tzs = new ArrayList();
            if (country == null) {
                return tzs;
            }
            XmlResourceParser parser = Resources.getSystem().getXml(R.xml.time_zones_by_country);
            try {
                XmlUtils.beginDocument(parser, "timezones");
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null || !element.equals("timezone")) {
                        parser.close();
                    } else if (country.equals(parser.getAttributeValue(null, "code")) && parser.next() == 4) {
                        TimeZone tz = TimeZone.getTimeZone(parser.getText());
                        if (!tz.getID().startsWith("GMT")) {
                            tzs.add(tz);
                        }
                    }
                }
                parser.close();
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Got xml parser exception getTimeZone('" + country + "'): e=", e);
            } catch (IOException e2) {
                Log.e(TAG, "Got IO exception getTimeZone('" + country + "'): e=", e2);
            } catch (Throwable th) {
                parser.close();
            }
            synchronized (sLastLockObj) {
                sLastZones = tzs;
                sLastCountry = country;
                arrayList = sLastZones;
            }
            return arrayList;
        }
    }

    public static String getTimeZoneDatabaseVersion() {
        return ZoneInfoDB.getInstance().getVersion();
    }

    private static int accumField(int amt, int suffix, boolean always, int zeropad) {
        if (amt > 999) {
            int num = 0;
            while (amt != 0) {
                num++;
                amt /= 10;
            }
            return num + suffix;
        } else if (amt > 99 || (always && zeropad >= 3)) {
            return suffix + 3;
        } else {
            if (amt > 9 || (always && zeropad >= 2)) {
                return suffix + 2;
            }
            if (always || amt > 0) {
                return suffix + 1;
            }
            return 0;
        }
    }

    private static int printFieldLocked(char[] formatStr, int amt, char suffix, int pos, boolean always, int zeropad) {
        if (!always && amt <= 0) {
            return pos;
        }
        int startPos = pos;
        if (amt > 999) {
            int tmp = 0;
            while (amt != 0 && tmp < sTmpFormatStr.length) {
                sTmpFormatStr[tmp] = (char) ((amt % 10) + 48);
                tmp++;
                amt /= 10;
            }
            for (tmp--; tmp >= 0; tmp--) {
                formatStr[pos] = sTmpFormatStr[tmp];
                pos++;
            }
        } else {
            int dig;
            if (!always || zeropad < 3) {
                if (amt > 99) {
                }
                if ((!always || zeropad < 2) && amt <= 9) {
                    if (startPos != pos) {
                    }
                    formatStr[pos] = (char) (amt + 48);
                    pos++;
                }
                dig = amt / 10;
                formatStr[pos] = (char) (dig + 48);
                pos++;
                amt -= dig * 10;
                formatStr[pos] = (char) (amt + 48);
                pos++;
            }
            dig = amt / 100;
            formatStr[pos] = (char) (dig + 48);
            pos++;
            amt -= dig * 100;
            if (startPos != pos) {
                dig = amt / 10;
                formatStr[pos] = (char) (dig + 48);
                pos++;
                amt -= dig * 10;
            }
            formatStr[pos] = (char) (amt + 48);
            pos++;
        }
        formatStr[pos] = suffix;
        return pos + 1;
    }

    private static int formatDurationLocked(long duration, int fieldLen) {
        if (sFormatStr.length < fieldLen) {
            sFormatStr = new char[fieldLen];
        }
        char[] formatStr = sFormatStr;
        if (duration == 0) {
            fieldLen--;
            int pos = 0;
            while (pos < fieldLen) {
                int pos2 = pos + 1;
                formatStr[pos] = ' ';
                pos = pos2;
            }
            formatStr[pos] = '0';
            return pos + 1;
        }
        char prefix;
        if (duration > 0) {
            prefix = '+';
        } else {
            prefix = '-';
            duration = -duration;
        }
        int millis = (int) (duration % 1000);
        int seconds = (int) Math.floor((double) (duration / 1000));
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (seconds >= SECONDS_PER_DAY) {
            days = seconds / SECONDS_PER_DAY;
            seconds -= SECONDS_PER_DAY * days;
        }
        if (seconds >= SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds >= SECONDS_PER_MINUTE) {
            minutes = seconds / SECONDS_PER_MINUTE;
            seconds -= minutes * SECONDS_PER_MINUTE;
        }
        pos2 = 0;
        if (fieldLen != 0) {
            int myLen = accumField(days, 1, DBG, 0);
            myLen += accumField(hours, 1, myLen > 0 ? true : DBG, 2);
            myLen += accumField(minutes, 1, myLen > 0 ? true : DBG, 2);
            myLen += accumField(seconds, 1, myLen > 0 ? true : DBG, 2);
            for (myLen += accumField(millis, 2, true, myLen > 0 ? 3 : 0) + 1; myLen < fieldLen; myLen++) {
                formatStr[pos2] = ' ';
                pos2++;
            }
        }
        formatStr[pos2] = prefix;
        pos2++;
        int start = pos2;
        boolean zeropad = fieldLen != 0 ? true : DBG;
        pos2 = printFieldLocked(formatStr, days, DateFormat.DATE, pos2, DBG, 0);
        pos2 = printFieldLocked(formatStr, hours, DateFormat.HOUR, pos2, pos2 != start ? true : DBG, zeropad ? 2 : 0);
        pos2 = printFieldLocked(formatStr, minutes, DateFormat.MINUTE, pos2, pos2 != start ? true : DBG, zeropad ? 2 : 0);
        pos2 = printFieldLocked(formatStr, seconds, DateFormat.SECONDS, pos2, pos2 != start ? true : DBG, zeropad ? 2 : 0);
        int i = (!zeropad || pos2 == start) ? 0 : 3;
        pos2 = printFieldLocked(formatStr, millis, DateFormat.MINUTE, pos2, true, i);
        formatStr[pos2] = DateFormat.SECONDS;
        return pos2 + 1;
    }

    public static void formatDuration(long duration, StringBuilder builder) {
        synchronized (sFormatSync) {
            builder.append(sFormatStr, 0, formatDurationLocked(duration, 0));
        }
    }

    public static void formatDuration(long duration, PrintWriter pw, int fieldLen) {
        synchronized (sFormatSync) {
            pw.print(new String(sFormatStr, 0, formatDurationLocked(duration, fieldLen)));
        }
    }

    public static void formatDuration(long duration, PrintWriter pw) {
        formatDuration(duration, pw, 0);
    }

    public static void formatDuration(long time, long now, PrintWriter pw) {
        if (time == 0) {
            pw.print("--");
        } else {
            formatDuration(time - now, pw, 0);
        }
    }

    public static String formatUptime(long time) {
        long diff = time - SystemClock.uptimeMillis();
        if (diff > 0) {
            return time + " (in " + diff + " ms)";
        }
        if (diff < 0) {
            return time + " (" + (-diff) + " ms ago)";
        }
        return time + " (now)";
    }

    public static String logTimeOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        if (millis < 0) {
            return Long.toString(millis);
        }
        c.setTimeInMillis(millis);
        return String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c});
    }

    public static String formatForLogging(long millis) {
        if (millis <= 0) {
            return SmsConstants.FORMAT_UNKNOWN;
        }
        return sLoggingFormat.format(new Date(millis));
    }
}
