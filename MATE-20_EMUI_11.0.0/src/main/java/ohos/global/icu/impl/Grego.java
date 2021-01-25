package ohos.global.icu.impl;

import ohos.devtools.JLogConstants;

public class Grego {
    private static final int[] DAYS_BEFORE = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, JLogConstants.JLID_CAMERA_CLOSECAMERA_END, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, JLogConstants.JLID_ACTIVITY_LAUNCHING_BEGIN};
    private static final int JULIAN_1970_CE = 2440588;
    private static final int JULIAN_1_CE = 1721426;
    public static final long MAX_MILLIS = 183882168921600000L;
    public static final int MILLIS_PER_DAY = 86400000;
    public static final int MILLIS_PER_HOUR = 3600000;
    public static final int MILLIS_PER_MINUTE = 60000;
    public static final int MILLIS_PER_SECOND = 1000;
    public static final long MIN_MILLIS = -184303902528000000L;
    private static final int[] MONTH_LENGTH = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public static final boolean isLeapYear(int i) {
        return (i & 3) == 0 && (i % 100 != 0 || i % 400 == 0);
    }

    public static final int monthLength(int i, int i2) {
        return MONTH_LENGTH[i2 + (isLeapYear(i) ? 12 : 0)];
    }

    public static final int previousMonthLength(int i, int i2) {
        if (i2 > 0) {
            return monthLength(i, i2 - 1);
        }
        return 31;
    }

    public static long fieldsToDay(int i, int i2, int i3) {
        int i4 = i - 1;
        long j = (long) i4;
        return (((((((((long) (i4 * JLogConstants.JLID_APP_FRONZED_BEGIN)) + floorDivide(j, 4)) + 1721423) + floorDivide(j, 400)) - floorDivide(j, 100)) + 2) + ((long) DAYS_BEFORE[i2 + (isLeapYear(i) ? 12 : 0)])) + ((long) i3)) - 2440588;
    }

    public static int dayOfWeek(long j) {
        long[] jArr = new long[1];
        floorDivide(j + 5, 7, jArr);
        int i = (int) jArr[0];
        if (i == 0) {
            return 7;
        }
        return i;
    }

    public static int[] dayToFields(long j, int[] iArr) {
        if (iArr == null || iArr.length < 5) {
            iArr = new int[5];
        }
        long j2 = j + 719162;
        long[] jArr = new long[1];
        long floorDivide = floorDivide(j2, 146097, jArr);
        long floorDivide2 = floorDivide(jArr[0], 36524, jArr);
        long floorDivide3 = floorDivide(jArr[0], 1461, jArr);
        long floorDivide4 = floorDivide(jArr[0], 365, jArr);
        int i = (int) ((floorDivide * 400) + (100 * floorDivide2) + (floorDivide3 * 4) + floorDivide4);
        int i2 = (int) jArr[0];
        if (floorDivide2 == 4 || floorDivide4 == 4) {
            i2 = JLogConstants.JLID_APP_FRONZED_BEGIN;
        } else {
            i++;
        }
        boolean isLeapYear = isLeapYear(i);
        int i3 = ((((i2 >= (isLeapYear ? 60 : 59) ? isLeapYear ? 1 : 2 : 0) + i2) * 12) + 6) / JLogConstants.JLID_APP_UNFRONZED_BEGIN;
        int i4 = (i2 - DAYS_BEFORE[isLeapYear ? i3 + 12 : i3]) + 1;
        int i5 = (int) ((j2 + 2) % 7);
        if (i5 < 1) {
            i5 += 7;
        }
        iArr[0] = i;
        iArr[1] = i3;
        iArr[2] = i4;
        iArr[3] = i5;
        iArr[4] = i2 + 1;
        return iArr;
    }

    public static int[] timeToFields(long j, int[] iArr) {
        if (iArr == null || iArr.length < 6) {
            iArr = new int[6];
        }
        long[] jArr = new long[1];
        dayToFields(floorDivide(j, CalendarAstronomer.DAY_MS, jArr), iArr);
        iArr[5] = (int) jArr[0];
        return iArr;
    }

    public static long floorDivide(long j, long j2) {
        if (j >= 0) {
            return j / j2;
        }
        return ((j + 1) / j2) - 1;
    }

    private static long floorDivide(long j, long j2, long[] jArr) {
        if (j >= 0) {
            jArr[0] = j % j2;
            return j / j2;
        }
        long j3 = ((j + 1) / j2) - 1;
        jArr[0] = j - (j2 * j3);
        return j3;
    }

    public static int getDayOfWeekInMonth(int i, int i2, int i3) {
        int i4 = (i3 + 6) / 7;
        if (i4 == 4) {
            if (i3 + 7 > monthLength(i, i2)) {
                return -1;
            }
        } else if (i4 == 5) {
            return -1;
        }
        return i4;
    }

    public static String timeToString(long j) {
        int[] timeToFields = timeToFields(j, null);
        int i = timeToFields[5];
        int i2 = i / 3600000;
        int i3 = i % 3600000;
        int i4 = i3 / 60000;
        int i5 = i3 % 60000;
        return String.format(null, "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ", Integer.valueOf(timeToFields[0]), Integer.valueOf(timeToFields[1] + 1), Integer.valueOf(timeToFields[2]), Integer.valueOf(i2), Integer.valueOf(i4), Integer.valueOf(i5 / 1000), Integer.valueOf(i5 % 1000));
    }
}
