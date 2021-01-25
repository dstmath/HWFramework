package com.huawei.gson.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PreJava9DateFormatProvider {
    public static DateFormat getUSDateFormat(int style) {
        return new SimpleDateFormat(getDateFormatPattern(style), Locale.US);
    }

    public static DateFormat getUSDateTimeFormat(int dateStyle, int timeStyle) {
        return new SimpleDateFormat(getDatePartOfDateTimePattern(dateStyle) + " " + getTimePartOfDateTimePattern(timeStyle), Locale.US);
    }

    private static String getDateFormatPattern(int style) {
        if (style == 0) {
            return "EEEE, MMMM d, y";
        }
        if (style == 1) {
            return "MMMM d, y";
        }
        if (style == 2) {
            return "MMM d, y";
        }
        if (style == 3) {
            return "M/d/yy";
        }
        throw new IllegalArgumentException("Unknown DateFormat style: " + style);
    }

    private static String getDatePartOfDateTimePattern(int dateStyle) {
        if (dateStyle == 0) {
            return "EEEE, MMMM d, yyyy";
        }
        if (dateStyle == 1) {
            return "MMMM d, yyyy";
        }
        if (dateStyle == 2) {
            return "MMM d, yyyy";
        }
        if (dateStyle == 3) {
            return "M/d/yy";
        }
        throw new IllegalArgumentException("Unknown DateFormat style: " + dateStyle);
    }

    private static String getTimePartOfDateTimePattern(int timeStyle) {
        if (timeStyle == 0 || timeStyle == 1) {
            return "h:mm:ss a z";
        }
        if (timeStyle == 2) {
            return "h:mm:ss a";
        }
        if (timeStyle == 3) {
            return "h:mm a";
        }
        throw new IllegalArgumentException("Unknown DateFormat style: " + timeStyle);
    }
}
