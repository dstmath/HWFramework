package android.support.v4.util;

import android.support.annotation.RestrictTo;
import java.io.PrintWriter;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public final class TimeUtils {
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final int HUNDRED_DAY_FIELD_LEN = 19;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static char[] sFormatStr = new char[24];
    private static final Object sFormatSync = new Object();

    private static int accumField(int amt, int suffix, boolean always, int zeropad) {
        if (amt > 99 || (always && zeropad >= 3)) {
            return 3 + suffix;
        }
        if (amt > 9 || (always && zeropad >= 2)) {
            return 2 + suffix;
        }
        if (always || amt > 0) {
            return 1 + suffix;
        }
        return 0;
    }

    private static int printField(char[] formatStr, int amt, char suffix, int pos, boolean always, int zeropad) {
        if (!always && amt <= 0) {
            return pos;
        }
        int startPos = pos;
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
        int pos2 = pos + 1;
        formatStr[pos2] = suffix;
        return pos2 + 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0132, code lost:
        if (r9 != r7) goto L_0x0139;
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
            int fieldLen2 = i - 1;
            while (0 < fieldLen2) {
                formatStr[0] = ' ';
            }
            formatStr[0] = '0';
            return 0 + 1;
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
        if (seconds2 > SECONDS_PER_DAY) {
            days = seconds2 / SECONDS_PER_DAY;
            seconds2 -= SECONDS_PER_DAY * days;
        }
        int days2 = days;
        if (seconds2 > SECONDS_PER_HOUR) {
            int hours2 = seconds2 / SECONDS_PER_HOUR;
            seconds2 -= hours2 * SECONDS_PER_HOUR;
            hours = hours2;
        } else {
            hours = 0;
        }
        if (seconds2 > SECONDS_PER_MINUTE) {
            int minutes2 = seconds2 / SECONDS_PER_MINUTE;
            seconds = seconds2 - (minutes2 * SECONDS_PER_MINUTE);
            minutes = minutes2;
        } else {
            seconds = seconds2;
            minutes = 0;
        }
        int pos = 0;
        int pos2 = 3;
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
                formatStr[pos] = ' ';
                pos++;
            }
        }
        formatStr[pos] = prefix;
        int pos3 = pos + 1;
        int start2 = pos3;
        boolean zeropad = i != 0;
        boolean z2 = true;
        int pos4 = 2;
        int pos5 = printField(formatStr, days2, 'd', pos3, false, 0);
        int start3 = start2;
        int i2 = pos5;
        int pos6 = printField(formatStr, hours, 'h', pos5, pos5 != start3, zeropad ? 2 : 0);
        int start4 = start3;
        int start5 = start4;
        int i3 = pos6;
        int pos7 = printField(formatStr, minutes, 'm', pos6, pos6 != start4, zeropad ? 2 : 0);
        int start6 = start5;
        if (pos7 == start6) {
            z2 = false;
        }
        if (!zeropad) {
            pos4 = 0;
        }
        int start7 = start6;
        int i4 = pos7;
        int pos8 = printField(formatStr, seconds, 's', pos7, z2, pos4);
        if (zeropad) {
            start = start7;
        } else {
            start = start7;
        }
        pos2 = 0;
        int i5 = start;
        int i6 = pos8;
        int pos9 = printField(formatStr, millis, 'm', pos8, true, pos2);
        formatStr[pos9] = 's';
        return pos9 + 1;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static void formatDuration(long duration, StringBuilder builder) {
        synchronized (sFormatSync) {
            builder.append(sFormatStr, 0, formatDurationLocked(duration, 0));
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static void formatDuration(long duration, PrintWriter pw, int fieldLen) {
        synchronized (sFormatSync) {
            pw.print(new String(sFormatStr, 0, formatDurationLocked(duration, fieldLen)));
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static void formatDuration(long duration, PrintWriter pw) {
        formatDuration(duration, pw, 0);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static void formatDuration(long time, long now, PrintWriter pw) {
        if (time == 0) {
            pw.print("--");
        } else {
            formatDuration(time - now, pw, 0);
        }
    }

    private TimeUtils() {
    }
}
