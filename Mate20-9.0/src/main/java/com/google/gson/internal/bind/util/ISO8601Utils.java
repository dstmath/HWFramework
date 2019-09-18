package com.google.gson.internal.bind.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class ISO8601Utils {
    private static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone(UTC_ID);
    private static final String UTC_ID = "UTC";

    public static String format(Date date) {
        return format(date, false, TIMEZONE_UTC);
    }

    public static String format(Date date, boolean millis) {
        return format(date, millis, TIMEZONE_UTC);
    }

    public static String format(Date date, boolean millis, TimeZone tz) {
        int length;
        Calendar calendar = new GregorianCalendar(tz, Locale.US);
        calendar.setTime(date);
        int capacity = "yyyy-MM-ddThh:mm:ss".length() + (millis ? ".sss".length() : 0);
        if (tz.getRawOffset() == 0) {
            length = "Z".length();
        } else {
            length = "+hh:mm".length();
        }
        StringBuilder formatted = new StringBuilder(capacity + length);
        padInt(formatted, calendar.get(1), "yyyy".length());
        formatted.append('-');
        padInt(formatted, calendar.get(2) + 1, "MM".length());
        formatted.append('-');
        padInt(formatted, calendar.get(5), "dd".length());
        formatted.append('T');
        padInt(formatted, calendar.get(11), "hh".length());
        formatted.append(':');
        padInt(formatted, calendar.get(12), "mm".length());
        formatted.append(':');
        padInt(formatted, calendar.get(13), "ss".length());
        if (millis) {
            formatted.append('.');
            padInt(formatted, calendar.get(14), "sss".length());
        }
        int offset = tz.getOffset(calendar.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / 60000) / 60);
            int minutes = Math.abs((offset / 60000) % 60);
            formatted.append(offset < 0 ? '-' : '+');
            padInt(formatted, hours, "hh".length());
            formatted.append(':');
            padInt(formatted, minutes, "mm".length());
        } else {
            formatted.append('Z');
        }
        return formatted.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0150  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0315  */
    public static Date parse(String date, ParsePosition pos) throws ParseException {
        Exception fail;
        String msg;
        int offset;
        int offset2;
        TimeZone timezone;
        int offset3;
        try {
            int offset4 = pos.getIndex();
            int offset5 = offset4 + 4;
            int year = parseInt(date, offset4, offset5);
            if (checkOffset(date, offset5, '-')) {
                offset5++;
            }
            int offset6 = offset5 + 2;
            int month = parseInt(date, offset5, offset6);
            if (checkOffset(date, offset6, '-')) {
                offset = offset6 + 1;
            } else {
                offset = offset6;
            }
            int offset7 = offset + 2;
            int day = parseInt(date, offset, offset7);
            int hour = 0;
            int minutes = 0;
            int seconds = 0;
            int milliseconds = 0;
            boolean hasT = checkOffset(date, offset7, 'T');
            if (hasT || date.length() > offset7) {
                if (hasT) {
                    int offset8 = offset7 + 1;
                    int offset9 = offset8 + 2;
                    hour = parseInt(date, offset8, offset9);
                    if (checkOffset(date, offset9, ':')) {
                        offset9++;
                    }
                    int offset10 = offset9 + 2;
                    minutes = parseInt(date, offset9, offset10);
                    if (checkOffset(date, offset10, ':')) {
                        offset3 = offset10 + 1;
                    } else {
                        offset3 = offset10;
                    }
                    if (date.length() > offset3) {
                        char c = date.charAt(offset3);
                        if (!(c == 'Z' || c == '+' || c == '-')) {
                            offset7 = offset3 + 2;
                            seconds = parseInt(date, offset3, offset7);
                            if (seconds > 59 && seconds < 63) {
                                seconds = 59;
                            }
                            if (checkOffset(date, offset7, '.')) {
                                int offset11 = offset7 + 1;
                                int endOffset = indexOfNonDigit(date, offset11 + 1);
                                int parseEndOffset = Math.min(endOffset, offset11 + 3);
                                int fraction = parseInt(date, offset11, parseEndOffset);
                                switch (parseEndOffset - offset11) {
                                    case 1:
                                        milliseconds = fraction * 100;
                                        break;
                                    case 2:
                                        milliseconds = fraction * 10;
                                        break;
                                    default:
                                        milliseconds = fraction;
                                        break;
                                }
                                offset7 = endOffset;
                            }
                        }
                    }
                    offset7 = offset3;
                }
                if (date.length() <= offset7) {
                    throw new IllegalArgumentException("No time zone indicator");
                }
                char timezoneIndicator = date.charAt(offset7);
                if (timezoneIndicator == 'Z') {
                    timezone = TIMEZONE_UTC;
                    offset2 = offset7 + 1;
                } else if (timezoneIndicator == '+' || timezoneIndicator == '-') {
                    String timezoneOffset = date.substring(offset7);
                    if (timezoneOffset.length() < 5) {
                        timezoneOffset = timezoneOffset + "00";
                    }
                    offset2 = offset7 + timezoneOffset.length();
                    if ("+0000".equals(timezoneOffset) || "+00:00".equals(timezoneOffset)) {
                        timezone = TIMEZONE_UTC;
                    } else {
                        String timezoneId = "GMT" + timezoneOffset;
                        timezone = TimeZone.getTimeZone(timezoneId);
                        String act = timezone.getID();
                        if (!act.equals(timezoneId) && !act.replace(":", "").equals(timezoneId)) {
                            throw new IndexOutOfBoundsException("Mismatching time zone indicator: " + timezoneId + " given, resolves to " + timezone.getID());
                        }
                    }
                } else {
                    throw new IndexOutOfBoundsException("Invalid time zone indicator '" + timezoneIndicator + "'");
                }
                Calendar calendar = new GregorianCalendar(timezone);
                calendar.setLenient(false);
                calendar.set(1, year);
                calendar.set(2, month - 1);
                calendar.set(5, day);
                calendar.set(11, hour);
                calendar.set(12, minutes);
                calendar.set(13, seconds);
                calendar.set(14, milliseconds);
                pos.setIndex(offset2);
                return calendar.getTime();
            }
            Calendar calendar2 = new GregorianCalendar(year, month - 1, day);
            pos.setIndex(offset7);
            return calendar2.getTime();
        } catch (IndexOutOfBoundsException e) {
            fail = e;
            String input = date == null ? null : '\"' + date + "'";
            msg = fail.getMessage();
            if (msg == null || msg.isEmpty()) {
                msg = "(" + fail.getClass().getName() + ")";
            }
            ParseException ex = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
            ex.initCause(fail);
            throw ex;
        } catch (NumberFormatException e2) {
            fail = e2;
            if (date == null) {
            }
            msg = fail.getMessage();
            msg = "(" + fail.getClass().getName() + ")";
            ParseException ex2 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
            ex2.initCause(fail);
            throw ex2;
        } catch (IllegalArgumentException e3) {
            fail = e3;
            if (date == null) {
            }
            msg = fail.getMessage();
            msg = "(" + fail.getClass().getName() + ")";
            ParseException ex22 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
            ex22.initCause(fail);
            throw ex22;
        }
    }

    private static boolean checkOffset(String value, int offset, char expected) {
        return offset < value.length() && value.charAt(offset) == expected;
    }

    private static int parseInt(String value, int beginIndex, int endIndex) throws NumberFormatException {
        int i;
        if (beginIndex < 0 || endIndex > value.length() || beginIndex > endIndex) {
            throw new NumberFormatException(value);
        }
        int i2 = beginIndex;
        int result = 0;
        if (i2 < endIndex) {
            i = i2 + 1;
            int digit = Character.digit(value.charAt(i2), 10);
            if (digit < 0) {
                throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
            }
            result = -digit;
        } else {
            i = i2;
        }
        while (i < endIndex) {
            int i3 = i + 1;
            int digit2 = Character.digit(value.charAt(i), 10);
            if (digit2 < 0) {
                throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
            }
            result = (result * 10) - digit2;
            i = i3;
        }
        return -result;
    }

    private static void padInt(StringBuilder buffer, int value, int length) {
        String strValue = Integer.toString(value);
        for (int i = length - strValue.length(); i > 0; i--) {
            buffer.append('0');
        }
        buffer.append(strValue);
    }

    private static int indexOfNonDigit(String string, int offset) {
        int i = offset;
        while (i < string.length()) {
            char c = string.charAt(i);
            if (c < '0' || c > '9') {
                return i;
            }
            i++;
        }
        return string.length();
    }
}
