package android.util;

import android.annotation.UnsupportedAppUsage;
import android.os.SystemClock;
import android.text.format.DateFormat;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import libcore.timezone.CountryTimeZones;
import libcore.timezone.TimeZoneFinder;
import libcore.timezone.ZoneInfoDB;

public class TimeUtils {
    public static final int HUNDRED_DAY_FIELD_LEN = 19;
    public static final long NANOS_PER_MS = 1000000;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    public static final SimpleDateFormat sDumpDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static char[] sFormatStr = new char[29];
    private static final Object sFormatSync = new Object();
    private static SimpleDateFormat sLoggingFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static char[] sTmpFormatStr = new char[29];

    public static TimeZone getTimeZone(int offset, boolean dst, long when, String country) {
        android.icu.util.TimeZone icuTimeZone = getIcuTimeZone(offset, dst, when, country);
        if (icuTimeZone != null) {
            return TimeZone.getTimeZone(icuTimeZone.getID());
        }
        return null;
    }

    private static android.icu.util.TimeZone getIcuTimeZone(int offset, boolean dst, long when, String country) {
        if (country == null) {
            return null;
        }
        return TimeZoneFinder.getInstance().lookupTimeZoneByCountryAndOffset(country, offset, dst, when, android.icu.util.TimeZone.getDefault());
    }

    public static List<String> getTimeZoneIdsForCountryCode(String countryCode) {
        if (countryCode != null) {
            CountryTimeZones countryTimeZones = TimeZoneFinder.getInstance().lookupCountryTimeZones(countryCode.toLowerCase());
            if (countryTimeZones == null) {
                return null;
            }
            List<String> timeZoneIds = new ArrayList<>();
            for (CountryTimeZones.TimeZoneMapping timeZoneMapping : countryTimeZones.getTimeZoneMappings()) {
                if (timeZoneMapping.showInPicker) {
                    timeZoneIds.add(timeZoneMapping.timeZoneId);
                }
            }
            return Collections.unmodifiableList(timeZoneIds);
        }
        throw new NullPointerException("countryCode == null");
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
        if (amt > 999) {
            int tmp = 0;
            while (amt != 0) {
                char[] cArr = sTmpFormatStr;
                if (tmp >= cArr.length) {
                    break;
                }
                cArr[tmp] = (char) ((amt % 10) + 48);
                tmp++;
                amt /= 10;
            }
            for (int tmp2 = tmp - 1; tmp2 >= 0; tmp2--) {
                formatStr[pos] = sTmpFormatStr[tmp2];
                pos++;
            }
        } else {
            if ((always && zeropad >= 3) || amt > 99) {
                int dig = amt / 100;
                formatStr[pos] = (char) (dig + 48);
                pos++;
                amt -= dig * 100;
            }
            if ((always && zeropad >= 2) || amt > 9 || pos != pos) {
                int dig2 = amt / 10;
                formatStr[pos] = (char) (dig2 + 48);
                pos++;
                amt -= dig2 * 10;
            }
            formatStr[pos] = (char) (amt + 48);
            pos++;
        }
        formatStr[pos] = suffix;
        return pos + 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0136, code lost:
        if (r9 != r7) goto L_0x013d;
     */
    private static int formatDurationLocked(long duration, int fieldLen) {
        char prefix;
        int days;
        int hours;
        int seconds;
        int minutes;
        int start;
        long duration2 = duration;
        if (sFormatStr.length < fieldLen) {
            sFormatStr = new char[fieldLen];
        }
        char[] formatStr = sFormatStr;
        if (duration2 == 0) {
            int pos = 0;
            int fieldLen2 = fieldLen - 1;
            while (pos < fieldLen2) {
                formatStr[pos] = ' ';
                pos++;
            }
            formatStr[pos] = '0';
            return pos + 1;
        }
        if (duration2 > 0) {
            prefix = '+';
        } else {
            duration2 = -duration2;
            prefix = '-';
        }
        int millis = (int) (duration2 % 1000);
        int seconds2 = (int) Math.floor((double) (duration2 / 1000));
        if (seconds2 >= SECONDS_PER_DAY) {
            int days2 = seconds2 / SECONDS_PER_DAY;
            seconds2 -= SECONDS_PER_DAY * days2;
            days = days2;
        } else {
            days = 0;
        }
        if (seconds2 >= 3600) {
            int hours2 = seconds2 / 3600;
            seconds2 -= hours2 * 3600;
            hours = hours2;
        } else {
            hours = 0;
        }
        if (seconds2 >= 60) {
            int minutes2 = seconds2 / 60;
            seconds = seconds2 - (minutes2 * 60);
            minutes = minutes2;
        } else {
            seconds = seconds2;
            minutes = 0;
        }
        int pos2 = 0;
        int i = 3;
        boolean z = false;
        if (fieldLen != 0) {
            int myLen = accumField(days, 1, false, 0);
            if (myLen > 0) {
                z = true;
            }
            int myLen2 = myLen + accumField(hours, 1, z, 2);
            int myLen3 = myLen2 + accumField(minutes, 1, myLen2 > 0, 2);
            int myLen4 = myLen3 + accumField(seconds, 1, myLen3 > 0, 2);
            for (int myLen5 = myLen4 + accumField(millis, 2, true, myLen4 > 0 ? 3 : 0) + 1; myLen5 < fieldLen; myLen5++) {
                formatStr[pos2] = ' ';
                pos2++;
            }
        }
        formatStr[pos2] = prefix;
        int pos3 = pos2 + 1;
        boolean zeropad = fieldLen != 0;
        boolean z2 = true;
        int i2 = 2;
        int pos4 = printFieldLocked(formatStr, days, DateFormat.DATE, pos3, false, 0);
        int pos5 = printFieldLocked(formatStr, hours, DateFormat.HOUR, pos4, pos4 != pos3, zeropad ? 2 : 0);
        int pos6 = printFieldLocked(formatStr, minutes, DateFormat.MINUTE, pos5, pos5 != pos3, zeropad ? 2 : 0);
        if (pos6 == pos3) {
            z2 = false;
        }
        if (!zeropad) {
            i2 = 0;
        }
        int pos7 = printFieldLocked(formatStr, seconds, 's', pos6, z2, i2);
        if (zeropad) {
            start = pos3;
        } else {
            start = pos3;
        }
        i = 0;
        int pos8 = printFieldLocked(formatStr, millis, DateFormat.MINUTE, pos7, true, i);
        formatStr[pos8] = 's';
        return pos8 + 1;
    }

    public static void formatDuration(long duration, StringBuilder builder) {
        synchronized (sFormatSync) {
            builder.append(sFormatStr, 0, formatDurationLocked(duration, 0));
        }
    }

    public static void formatDuration(long duration, StringBuilder builder, int fieldLen) {
        synchronized (sFormatSync) {
            builder.append(sFormatStr, 0, formatDurationLocked(duration, fieldLen));
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static void formatDuration(long duration, PrintWriter pw, int fieldLen) {
        synchronized (sFormatSync) {
            pw.print(new String(sFormatStr, 0, formatDurationLocked(duration, fieldLen)));
        }
    }

    public static String formatDuration(long duration) {
        String str;
        synchronized (sFormatSync) {
            str = new String(sFormatStr, 0, formatDurationLocked(duration, 0));
        }
        return str;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
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
        } else if (diff < 0) {
            return time + " (" + (-diff) + " ms ago)";
        } else {
            return time + " (now)";
        }
    }

    @UnsupportedAppUsage
    public static String logTimeOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        if (millis < 0) {
            return Long.toString(millis);
        }
        c.setTimeInMillis(millis);
        return String.format("%tm-%td %tH:%tM:%tS.%tL", c, c, c, c, c, c);
    }

    public static String formatForLogging(long millis) {
        if (millis <= 0) {
            return "unknown";
        }
        return sLoggingFormat.format(new Date(millis));
    }

    public static void dumpTime(PrintWriter pw, long time) {
        pw.print(sDumpDateFormat.format(new Date(time)));
    }

    public static void dumpTimeWithDelta(PrintWriter pw, long time, long now) {
        pw.print(sDumpDateFormat.format(new Date(time)));
        if (time == now) {
            pw.print(" (now)");
            return;
        }
        pw.print(" (");
        formatDuration(time, now, pw);
        pw.print(")");
    }
}
