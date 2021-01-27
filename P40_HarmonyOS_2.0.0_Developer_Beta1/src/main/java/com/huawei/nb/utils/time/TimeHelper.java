package com.huawei.nb.utils.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeHelper {
    public static final long DAY_TIME_IN_MS = 86400000;
    public static final String TIME_FORMAT_PATTEN = "^([2][0-3]|[0-1][0-9]):([0-5][0-9]):([0-5][0-9])$";
    public static final int TIME_UNIT_DAY = 4;
    public static final int TIME_UNIT_MONTH = 2;
    public static final int TIME_UNIT_WEEK = 3;
    public static final int TIME_UNIT_YEAR = 1;

    private static long getMilliSecs(int i, int i2) {
        long j;
        if (i > 0) {
            if (i2 == 1) {
                j = 31536000000L;
            } else if (i2 == 2) {
                j = 2592000000L;
            } else if (i2 == 3) {
                j = 604800000;
            } else if (i2 == 4) {
                return 86400000;
            }
            return ((long) i) * j;
        }
        return 0;
    }

    public static long now() {
        return System.nanoTime();
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long getTimeMillis(String str) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yy-MM-dd");
            return simpleDateFormat.parse(simpleDateFormat2.format(new Date()) + " " + str).getTime();
        } catch (ParseException unused) {
            return 0;
        }
    }

    public static boolean isTimeExpired(long j, long j2, int i, int i2) {
        if (j <= 0 || i <= 0) {
            return false;
        }
        return j2 - j > getMilliSecs(i, i2);
    }

    public static int getDiffTimeInterval(String str, String str2) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar instance = Calendar.getInstance();
        instance.setTime(simpleDateFormat.parse(str));
        long timeInMillis = instance.getTimeInMillis();
        instance.setTime(simpleDateFormat.parse(str2));
        return Integer.parseInt(String.valueOf((instance.getTimeInMillis() - timeInMillis) / 86400000));
    }

    public static String getStringDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
