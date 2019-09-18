package com.huawei.odmf.utils;

import com.huawei.odmf.exception.ODMFException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ThreadLocalDateUtil {
    private static DateFormat dateFormat = null;
    private static final String date_format = "yyyy-MM-dd HH:mm:ss.S";
    private static final Object lock = new Object();
    private static DateFormat timeFormat = null;
    private static final String time_format = "HH:mm:ss";

    public static Date parseDate(String strDate) {
        Date parse;
        synchronized (lock) {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(date_format);
            }
            try {
                parse = dateFormat.parse(strDate);
            } catch (ParseException e) {
                throw new ODMFException("error happens when parsing date");
            }
        }
        return parse;
    }

    public static Time parseTime(String strDate) {
        Time time;
        synchronized (lock) {
            if (timeFormat == null) {
                timeFormat = new SimpleDateFormat(time_format);
            }
            try {
                time = new Time(timeFormat.parse(strDate).getTime());
            } catch (ParseException e) {
                throw new ODMFException("error happens when parsing time");
            }
        }
        return time;
    }

    public static Timestamp parseTimestamp(String strDate) {
        Timestamp timestamp;
        synchronized (lock) {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(date_format);
            }
            try {
                timestamp = new Timestamp(dateFormat.parse(strDate).getTime());
            } catch (ParseException e) {
                throw new ODMFException("error happens when parsing time");
            }
        }
        return timestamp;
    }

    public static Calendar parseCalendar(String strCalendar) {
        Calendar calendar;
        synchronized (lock) {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(date_format);
            }
            try {
                calendar = Calendar.getInstance();
                calendar.setTime(dateFormat.parse(strCalendar));
            } catch (ParseException e) {
                throw new ODMFException("error happens when parsing time");
            }
        }
        return calendar;
    }
}
