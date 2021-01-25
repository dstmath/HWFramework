package org.ksoap2.kobjects.isodate;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class IsoDate {
    public static final int DATE = 1;
    public static final int DATE_TIME = 3;
    public static final int TIME = 2;

    static void dd(StringBuffer buf, int i) {
        buf.append((char) ((i / 10) + 48));
        buf.append((char) ((i % 10) + 48));
    }

    public static String dateToString(Date date, int type) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        c.setTime(date);
        StringBuffer buf = new StringBuffer();
        if ((type & 1) != 0) {
            int year = c.get(1);
            dd(buf, year / 100);
            dd(buf, year % 100);
            buf.append('-');
            dd(buf, c.get(2) + 0 + 1);
            buf.append('-');
            dd(buf, c.get(5));
            if (type == 3) {
                buf.append("T");
            }
        }
        if ((type & 2) != 0) {
            dd(buf, c.get(11));
            buf.append(':');
            dd(buf, c.get(12));
            buf.append(':');
            dd(buf, c.get(13));
            buf.append('.');
            int ms = c.get(14);
            buf.append((char) ((ms / 100) + 48));
            dd(buf, ms % 100);
            buf.append('Z');
        }
        return buf.toString();
    }

    public static Date stringToDate(String text, int type) {
        Calendar c = Calendar.getInstance();
        if ((type & 1) != 0) {
            c.set(1, Integer.parseInt(text.substring(0, 4)));
            c.set(2, (Integer.parseInt(text.substring(5, 7)) - 1) + 0);
            c.set(5, Integer.parseInt(text.substring(8, 10)));
            if (type != 3 || text.length() < 11) {
                c.set(11, 0);
                c.set(12, 0);
                c.set(13, 0);
                c.set(14, 0);
                return c.getTime();
            }
            text = text.substring(11);
        } else {
            c.setTime(new Date(0));
        }
        c.set(11, Integer.parseInt(text.substring(0, 2)));
        c.set(12, Integer.parseInt(text.substring(3, 5)));
        c.set(13, Integer.parseInt(text.substring(6, 8)));
        int pos = 8;
        if (8 >= text.length() || text.charAt(8) != '.') {
            c.set(14, 0);
        } else {
            int ms = 0;
            int f = 100;
            while (true) {
                pos++;
                char d = text.charAt(pos);
                if (d < '0' || d > '9') {
                    break;
                }
                ms += (d - '0') * f;
                f /= 10;
            }
            c.set(14, ms);
        }
        if (pos < text.length()) {
            if (text.charAt(pos) == '+' || text.charAt(pos) == '-') {
                c.setTimeZone(TimeZone.getTimeZone("GMT" + text.substring(pos)));
            } else if (text.charAt(pos) == 'Z') {
                c.setTimeZone(TimeZone.getTimeZone("GMT"));
            } else {
                throw new RuntimeException("illegal time format!");
            }
        }
        return c.getTime();
    }
}
