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
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
    private static final Object LOCK = new Object();
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static DateFormat dateFormat;
    private static DateFormat timeFormat;

    private ThreadLocalDateUtil() {
    }

    public static Date parseDate(String str) {
        Date parse;
        synchronized (LOCK) {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(DATE_FORMAT);
            }
            try {
                parse = dateFormat.parse(str);
            } catch (ParseException unused) {
                throw new ODMFException("error happens when parsing date");
            }
        }
        return parse;
    }

    public static Time parseTime(String str) {
        Time time;
        synchronized (LOCK) {
            if (timeFormat == null) {
                timeFormat = new SimpleDateFormat(TIME_FORMAT);
            }
            try {
                time = new Time(timeFormat.parse(str).getTime());
            } catch (ParseException unused) {
                throw new ODMFException("error happens when parsing time");
            }
        }
        return time;
    }

    public static Timestamp parseTimestamp(String str) {
        Timestamp timestamp;
        synchronized (LOCK) {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(DATE_FORMAT);
            }
            try {
                timestamp = new Timestamp(dateFormat.parse(str).getTime());
            } catch (ParseException unused) {
                throw new ODMFException("error happens when parsing time");
            }
        }
        return timestamp;
    }

    public static Calendar parseCalendar(String str) {
        Calendar instance;
        synchronized (LOCK) {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(DATE_FORMAT);
            }
            try {
                instance = Calendar.getInstance();
                instance.setTime(dateFormat.parse(str));
            } catch (ParseException unused) {
                throw new ODMFException("error happens when parsing time");
            }
        }
        return instance;
    }
}
