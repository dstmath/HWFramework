package android.util;

import android.os.SystemClock;
import android.text.format.DateFormat;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import libcore.util.TimeZoneFinder;
import libcore.util.ZoneInfoDB;

public class TimeUtils {
    public static final int HUNDRED_DAY_FIELD_LEN = 19;
    public static final long NANOS_PER_MS = 1000000;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
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

    public static String getTimeZoneDatabaseVersion() {
        return ZoneInfoDB.getInstance().getVersion();
    }

    private static int accumField(int amt, int suffix, boolean always, int zeropad) {
        int num = 0;
        if (amt > 999) {
            while (amt != 0) {
                num++;
                amt /= 10;
            }
            return num + suffix;
        } else if (amt > 99 || (always && zeropad >= 3)) {
            return 3 + suffix;
        } else {
            if (amt > 9 || (always && zeropad >= 2)) {
                return 2 + suffix;
            }
            if (always || amt > 0) {
                return 1 + suffix;
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
            if ((always && zeropad >= 2) || amt > 9 || startPos != pos) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0135, code lost:
        if (r9 != r7) goto L_0x013c;
     */
    private static int formatDurationLocked(long duration, int fieldLen) {
        char c;
        int hours;
        int seconds;
        int minutes;
        int start;
        long duration2 = duration;
        int i = fieldLen;
        if (sFormatStr.length < i) {
            sFormatStr = new char[i];
        }
        char[] formatStr = sFormatStr;
        if (duration2 == 0) {
            int pos = 0;
            int fieldLen2 = i - 1;
            while (pos < fieldLen2) {
                formatStr[pos] = ' ';
                pos++;
            }
            formatStr[pos] = '0';
            return pos + 1;
        }
        if (duration2 > 0) {
            c = '+';
        } else {
            c = '-';
            duration2 = -duration2;
        }
        char prefix = c;
        int millis = (int) (duration2 % 1000);
        int seconds2 = (int) Math.floor((double) (duration2 / 1000));
        int days = 0;
        if (seconds2 >= SECONDS_PER_DAY) {
            days = seconds2 / SECONDS_PER_DAY;
            seconds2 -= SECONDS_PER_DAY * days;
        }
        int days2 = days;
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
        int pos3 = 3;
        boolean z = false;
        if (i != 0) {
            int myLen = accumField(days2, 1, false, 0);
            if (myLen > 0) {
                z = true;
            }
            int myLen2 = myLen + accumField(hours, 1, z, 2);
            int myLen3 = myLen2 + accumField(minutes, 1, myLen2 > 0, 2);
            int myLen4 = myLen3 + accumField(seconds, 1, myLen3 > 0, 2);
            for (int myLen5 = myLen4 + accumField(millis, 2, true, myLen4 > 0 ? 3 : 0) + 1; myLen5 < i; myLen5++) {
                formatStr[pos2] = ' ';
                pos2++;
            }
        }
        formatStr[pos2] = prefix;
        int pos4 = pos2 + 1;
        int start2 = pos4;
        boolean zeropad = i != 0;
        boolean z2 = true;
        int pos5 = 2;
        int pos6 = printFieldLocked(formatStr, days2, DateFormat.DATE, pos4, false, 0);
        int start3 = start2;
        int i2 = pos6;
        int pos7 = printFieldLocked(formatStr, hours, DateFormat.HOUR, pos6, pos6 != start3, zeropad ? 2 : 0);
        int start4 = start3;
        int start5 = start4;
        int i3 = pos7;
        int pos8 = printFieldLocked(formatStr, minutes, DateFormat.MINUTE, pos7, pos7 != start4, zeropad ? 2 : 0);
        int start6 = start5;
        if (pos8 == start6) {
            z2 = false;
        }
        if (!zeropad) {
            pos5 = 0;
        }
        int start7 = start6;
        int i4 = pos8;
        int pos9 = printFieldLocked(formatStr, seconds, DateFormat.SECONDS, pos8, z2, pos5);
        if (zeropad) {
            start = start7;
        } else {
            start = start7;
        }
        pos3 = 0;
        int i5 = start;
        int i6 = pos9;
        int pos10 = printFieldLocked(formatStr, millis, DateFormat.MINUTE, pos9, true, pos3);
        formatStr[pos10] = DateFormat.SECONDS;
        return pos10 + 1;
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
            return "unknown";
        }
        return sLoggingFormat.format(new Date(millis));
    }
}
