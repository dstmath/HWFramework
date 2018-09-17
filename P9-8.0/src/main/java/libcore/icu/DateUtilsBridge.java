package libcore.icu;

import android.icu.impl.JavaTimeZone;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

public final class DateUtilsBridge {
    public static final int FORMAT_12HOUR = 64;
    public static final int FORMAT_24HOUR = 128;
    public static final int FORMAT_ABBREV_ALL = 524288;
    public static final int FORMAT_ABBREV_MONTH = 65536;
    public static final int FORMAT_ABBREV_RELATIVE = 262144;
    public static final int FORMAT_ABBREV_TIME = 16384;
    public static final int FORMAT_ABBREV_WEEKDAY = 32768;
    public static final int FORMAT_NO_MONTH_DAY = 32;
    public static final int FORMAT_NO_YEAR = 8;
    public static final int FORMAT_NUMERIC_DATE = 131072;
    public static final int FORMAT_SHOW_DATE = 16;
    public static final int FORMAT_SHOW_TIME = 1;
    public static final int FORMAT_SHOW_WEEKDAY = 2;
    public static final int FORMAT_SHOW_YEAR = 4;
    public static final int FORMAT_UTC = 8192;

    public static TimeZone icuTimeZone(java.util.TimeZone tz) {
        JavaTimeZone javaTimeZone = new JavaTimeZone(tz, null);
        javaTimeZone.freeze();
        return javaTimeZone;
    }

    public static Calendar createIcuCalendar(TimeZone icuTimeZone, ULocale icuLocale, long timeInMillis) {
        Calendar calendar = new GregorianCalendar(icuTimeZone, icuLocale);
        calendar.setTimeInMillis(timeInMillis);
        return calendar;
    }

    public static String toSkeleton(Calendar calendar, int flags) {
        return toSkeleton(calendar, calendar, flags);
    }

    public static String toSkeleton(Calendar startCalendar, Calendar endCalendar, int flags) {
        boolean z = false;
        if ((524288 & flags) != 0) {
            flags |= 114688;
        }
        String monthPart = DateFormat.MONTH;
        if ((131072 & flags) != 0) {
            monthPart = DateFormat.NUM_MONTH;
        } else if ((65536 & flags) != 0) {
            monthPart = DateFormat.ABBR_MONTH;
        }
        String weekPart = DateFormat.WEEKDAY;
        if ((32768 & flags) != 0) {
            weekPart = "EEE";
        }
        String timePart = DateFormat.HOUR;
        if ((flags & 128) != 0) {
            timePart = DateFormat.HOUR24;
        } else if ((flags & 64) != 0) {
            timePart = "h";
        }
        if ((flags & 16384) == 0 || (flags & 128) != 0) {
            timePart = timePart + DateFormat.MINUTE;
        } else {
            if (onTheHour(startCalendar)) {
                z = onTheHour(endCalendar);
            }
            if (!z) {
                timePart = timePart + DateFormat.MINUTE;
            }
        }
        if (fallOnDifferentDates(startCalendar, endCalendar)) {
            flags |= 16;
        }
        if (fallInSameMonth(startCalendar, endCalendar) && (flags & 32) != 0) {
            flags = (flags & -3) & -2;
        }
        if ((flags & 19) == 0) {
            flags |= 16;
        }
        if ((flags & 16) != 0 && (flags & 4) == 0 && (flags & 8) == 0 && !(fallInSameYear(startCalendar, endCalendar) && (isThisYear(startCalendar) ^ 1) == 0)) {
            flags |= 4;
        }
        StringBuilder builder = new StringBuilder();
        if ((flags & 48) != 0) {
            if ((flags & 4) != 0) {
                builder.append(DateFormat.YEAR);
            }
            builder.append(monthPart);
            if ((flags & 32) == 0) {
                builder.append(DateFormat.DAY);
            }
        }
        if ((flags & 2) != 0) {
            builder.append(weekPart);
        }
        if ((flags & 1) != 0) {
            builder.append(timePart);
        }
        return builder.toString();
    }

    public static int dayDistance(Calendar c1, Calendar c2) {
        return c2.get(20) - c1.get(20);
    }

    private static boolean onTheHour(Calendar c) {
        return c.get(12) == 0 && c.get(13) == 0;
    }

    private static boolean fallOnDifferentDates(Calendar c1, Calendar c2) {
        if (c1.get(1) == c2.get(1) && c1.get(2) == c2.get(2) && c1.get(5) == c2.get(5)) {
            return false;
        }
        return true;
    }

    private static boolean fallInSameMonth(Calendar c1, Calendar c2) {
        return c1.get(2) == c2.get(2);
    }

    private static boolean fallInSameYear(Calendar c1, Calendar c2) {
        return c1.get(1) == c2.get(1);
    }

    private static boolean isThisYear(Calendar c) {
        Calendar now = (Calendar) c.clone();
        now.setTimeInMillis(System.currentTimeMillis());
        if (c.get(1) == now.get(1)) {
            return true;
        }
        return false;
    }
}
