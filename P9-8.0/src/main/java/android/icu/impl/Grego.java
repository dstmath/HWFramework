package android.icu.impl;

import android.icu.lang.UCharacter.UnicodeBlock;
import java.util.Locale;

public class Grego {
    private static final int[] DAYS_BEFORE = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, UnicodeBlock.TANGUT_COMPONENTS_ID, 304, 334, 0, 31, 60, 91, 121, 152, 182, 213, 244, UnicodeBlock.COUNT, 305, 335};
    private static final int JULIAN_1970_CE = 2440588;
    private static final int JULIAN_1_CE = 1721426;
    public static final long MAX_MILLIS = 183882168921600000L;
    public static final int MILLIS_PER_DAY = 86400000;
    public static final int MILLIS_PER_HOUR = 3600000;
    public static final int MILLIS_PER_MINUTE = 60000;
    public static final int MILLIS_PER_SECOND = 1000;
    public static final long MIN_MILLIS = -184303902528000000L;
    private static final int[] MONTH_LENGTH = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public static final boolean isLeapYear(int year) {
        return (year & 3) == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    public static final int monthLength(int year, int month) {
        return MONTH_LENGTH[(isLeapYear(year) ? 12 : 0) + month];
    }

    public static final int previousMonthLength(int year, int month) {
        return month > 0 ? monthLength(year, month - 1) : 31;
    }

    public static long fieldsToDay(int year, int month, int dom) {
        int y = year - 1;
        return (((((((((long) (y * 365)) + floorDivide((long) y, 4)) + 1721423) + floorDivide((long) y, 400)) - floorDivide((long) y, 100)) + 2) + ((long) DAYS_BEFORE[(isLeapYear(year) ? 12 : 0) + month])) + ((long) dom)) - 2440588;
    }

    public static int dayOfWeek(long day) {
        long[] remainder = new long[1];
        floorDivide(5 + day, 7, remainder);
        int dayOfWeek = (int) remainder[0];
        if (dayOfWeek == 0) {
            return 7;
        }
        return dayOfWeek;
    }

    public static int[] dayToFields(long day, int[] fields) {
        int i;
        if (fields == null || fields.length < 5) {
            fields = new int[5];
        }
        day += 719162;
        long[] rem = new long[1];
        long n400 = floorDivide(day, 146097, rem);
        long n100 = floorDivide(rem[0], 36524, rem);
        long n4 = floorDivide(rem[0], 1461, rem);
        long n1 = floorDivide(rem[0], 365, rem);
        int year = (int) ((((400 * n400) + (100 * n100)) + (4 * n4)) + n1);
        int dayOfYear = (int) rem[0];
        if (n100 == 4 || n1 == 4) {
            dayOfYear = 365;
        } else {
            year++;
        }
        boolean isLeap = isLeapYear(year);
        int correction = 0;
        if (dayOfYear >= (isLeap ? 60 : 59)) {
            correction = isLeap ? 1 : 2;
        }
        int month = (((dayOfYear + correction) * 12) + 6) / 367;
        int[] iArr = DAYS_BEFORE;
        if (isLeap) {
            i = month + 12;
        } else {
            i = month;
        }
        int dayOfMonth = (dayOfYear - iArr[i]) + 1;
        int dayOfWeek = (int) ((2 + day) % 7);
        if (dayOfWeek < 1) {
            dayOfWeek += 7;
        }
        dayOfYear++;
        fields[0] = year;
        fields[1] = month;
        fields[2] = dayOfMonth;
        fields[3] = dayOfWeek;
        fields[4] = dayOfYear;
        return fields;
    }

    public static int[] timeToFields(long time, int[] fields) {
        if (fields == null || fields.length < 6) {
            fields = new int[6];
        }
        long[] remainder = new long[1];
        dayToFields(floorDivide(time, 86400000, remainder), fields);
        fields[5] = (int) remainder[0];
        return fields;
    }

    public static long floorDivide(long numerator, long denominator) {
        if (numerator >= 0) {
            return numerator / denominator;
        }
        return ((numerator + 1) / denominator) - 1;
    }

    private static long floorDivide(long numerator, long denominator, long[] remainder) {
        if (numerator >= 0) {
            remainder[0] = numerator % denominator;
            return numerator / denominator;
        }
        long quotient = ((numerator + 1) / denominator) - 1;
        remainder[0] = numerator - (quotient * denominator);
        return quotient;
    }

    public static int getDayOfWeekInMonth(int year, int month, int dayOfMonth) {
        int weekInMonth = (dayOfMonth + 6) / 7;
        if (weekInMonth == 4) {
            if (dayOfMonth + 7 > monthLength(year, month)) {
                return -1;
            }
            return weekInMonth;
        } else if (weekInMonth == 5) {
            return -1;
        } else {
            return weekInMonth;
        }
    }

    public static String timeToString(long time) {
        int millis = timeToFields(time, null)[5];
        int hour = millis / 3600000;
        millis %= 3600000;
        int min = millis / 60000;
        millis %= 60000;
        int sec = millis / 1000;
        millis %= 1000;
        return String.format((Locale) null, "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ", new Object[]{Integer.valueOf(fields[0]), Integer.valueOf(fields[1] + 1), Integer.valueOf(fields[2]), Integer.valueOf(hour), Integer.valueOf(min), Integer.valueOf(sec), Integer.valueOf(millis)});
    }
}
