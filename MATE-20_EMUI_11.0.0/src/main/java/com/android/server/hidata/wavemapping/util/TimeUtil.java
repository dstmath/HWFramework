package com.android.server.hidata.wavemapping.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    public static final String DATE_FORMAT = "yyMMdd";
    private static final String DATE_PATTERN = "yyyyMMdd HH:mm:ss.SSS";
    private static final String DATE_PATTERN_02 = "MMddHHmmss";
    private static final String TAG = ("WMapping." + TimeUtil.class.getSimpleName());

    public String getTimePattern02() {
        return new SimpleDateFormat(DATE_PATTERN_02, Locale.getDefault()).format(new Date());
    }

    public int getTimeIntPattern02() {
        try {
            return Integer.parseInt(new SimpleDateFormat(DATE_PATTERN_02, Locale.getDefault()).format(new Date()));
        } catch (NumberFormatException e) {
            LogUtil.e(false, "getTimeIntPattern02:%{public}s", e.getMessage());
            return 0;
        }
    }

    public String getTime(long time, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(time));
    }

    public static String getTime() {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date());
    }

    public String changeDateFormat(String strDate) {
        if (strDate == null || "".equals(strDate)) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        try {
            return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(dateFormat.parse(strDate));
        } catch (ParseException e) {
            LogUtil.e(false, "changeDateFormat failed by ParseException", new Object[0]);
            return null;
        }
    }

    public String getSomeDay(Date date, int day) {
        if (date == null) {
            return null;
        }
        try {
            SimpleDateFormat dateFormat2 = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(5, day);
            return dateFormat2.format(calendar.getTime());
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getSomeDay failed by Exception", new Object[0]);
            return null;
        }
    }

    public int time2IntDate(String strDate) {
        if (strDate == null || "".equals(strDate)) {
            return 0;
        }
        try {
            return Integer.parseInt(changeDateFormat(strDate));
        } catch (NumberFormatException e) {
            LogUtil.e(false, "time2IntDate exception,%{public}s", strDate);
            return 0;
        }
    }

    public int timeStamp2DateInt(String timestampString) {
        if (timestampString == null || "".equals(timestampString)) {
            return 0;
        }
        try {
            return Integer.parseInt(new SimpleDateFormat(DATE_PATTERN_02, Locale.CHINA).format(new Date(Long.valueOf(Long.parseLong(timestampString)).longValue())));
        } catch (NumberFormatException e) {
            LogUtil.e(false, "timeStamp2DateInt exception,%{public}s", timestampString);
            return 0;
        }
    }
}
