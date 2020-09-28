package com.huawei.gson.internal.bind.util;

import com.android.internal.telephony.IccCardConstantsEx;
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
        Calendar calendar = new GregorianCalendar(tz, Locale.US);
        calendar.setTime(date);
        StringBuilder formatted = new StringBuilder("yyyy-MM-ddThh:mm:ss".length() + (millis ? ".sss".length() : 0) + (tz.getRawOffset() == 0 ? "Z" : "+hh:mm").length());
        padInt(formatted, calendar.get(1), "yyyy".length());
        char c = '-';
        formatted.append('-');
        padInt(formatted, calendar.get(2) + 1, "MM".length());
        formatted.append('-');
        padInt(formatted, calendar.get(5), "dd".length());
        formatted.append('T');
        padInt(formatted, calendar.get(11), "hh".length());
        formatted.append(':');
        padInt(formatted, calendar.get(12), "mm".length());
        formatted.append(':');
        padInt(formatted, calendar.get(13), IccCardConstantsEx.INTENT_KEY_ICC_STATE.length());
        if (millis) {
            formatted.append('.');
            padInt(formatted, calendar.get(14), "sss".length());
        }
        int offset = tz.getOffset(calendar.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / 60000) / 60);
            int minutes = Math.abs((offset / 60000) % 60);
            if (offset >= 0) {
                c = '+';
            }
            formatted.append(c);
            padInt(formatted, hours, "hh".length());
            formatted.append(':');
            padInt(formatted, minutes, "mm".length());
        } else {
            formatted.append('Z');
        }
        return formatted.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x0225  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0227  */
    public static Date parse(String date, ParsePosition pos) throws ParseException {
        NumberFormatException fail;
        String input;
        String msg;
        int offset;
        TimeZone timezone;
        String timezoneOffset;
        int offset2;
        try {
            int offset3 = pos.getIndex();
            int offset4 = offset3 + 4;
            int year = parseInt(date, offset3, offset4);
            if (checkOffset(date, offset4, '-')) {
                offset4++;
            }
            int offset5 = offset4 + 2;
            int month = parseInt(date, offset4, offset5);
            if (checkOffset(date, offset5, '-')) {
                offset5++;
            }
            int offset6 = offset5 + 2;
            int day = parseInt(date, offset5, offset6);
            int hour = 0;
            int minutes = 0;
            int seconds = 0;
            int milliseconds = 0;
            boolean hasT = checkOffset(date, offset6, 'T');
            if (!hasT) {
                try {
                    if (date.length() <= offset6) {
                        Calendar calendar = new GregorianCalendar(year, month - 1, day);
                        pos.setIndex(offset6);
                        return calendar.getTime();
                    }
                } catch (IndexOutOfBoundsException e) {
                    e = e;
                    fail = e;
                    if (date == null) {
                        input = null;
                    } else {
                        input = '\"' + date + '\"';
                    }
                    msg = fail.getMessage();
                    if (msg == null || msg.isEmpty()) {
                        msg = "(" + fail.getClass().getName() + ")";
                    }
                    ParseException ex = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
                    ex.initCause(fail);
                    throw ex;
                } catch (NumberFormatException e2) {
                    e = e2;
                    fail = e;
                    if (date == null) {
                    }
                    msg = fail.getMessage();
                    msg = "(" + fail.getClass().getName() + ")";
                    ParseException ex2 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
                    ex2.initCause(fail);
                    throw ex2;
                } catch (IllegalArgumentException e3) {
                    e = e3;
                    fail = e;
                    if (date == null) {
                    }
                    msg = fail.getMessage();
                    msg = "(" + fail.getClass().getName() + ")";
                    ParseException ex22 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
                    ex22.initCause(fail);
                    throw ex22;
                }
            }
            if (hasT) {
                int offset7 = offset6 + 1;
                int offset8 = offset7 + 2;
                hour = parseInt(date, offset7, offset8);
                if (checkOffset(date, offset8, ':')) {
                    offset8++;
                }
                int offset9 = offset8 + 2;
                minutes = parseInt(date, offset8, offset9);
                if (checkOffset(date, offset9, ':')) {
                    offset6 = offset9 + 1;
                } else {
                    offset6 = offset9;
                }
                if (date.length() > offset6) {
                    char c = date.charAt(offset6);
                    if (c != 'Z' && c != '+' && c != '-') {
                        int offset10 = offset6 + 2;
                        int seconds2 = parseInt(date, offset6, offset10);
                        if (seconds2 <= 59 || seconds2 >= 63) {
                            seconds = seconds2;
                        } else {
                            seconds = 59;
                        }
                        if (checkOffset(date, offset10, '.')) {
                            int offset11 = offset10 + 1;
                            offset6 = indexOfNonDigit(date, offset11 + 1);
                            int parseEndOffset = Math.min(offset6, offset11 + 3);
                            int fraction = parseInt(date, offset11, parseEndOffset);
                            int i = parseEndOffset - offset11;
                            if (i == 1) {
                                milliseconds = fraction * 100;
                            } else if (i != 2) {
                                milliseconds = fraction;
                            } else {
                                milliseconds = fraction * 10;
                            }
                        } else {
                            offset6 = offset10;
                        }
                    }
                }
            }
            try {
                if (date.length() > offset6) {
                    char timezoneIndicator = date.charAt(offset6);
                    if (timezoneIndicator == 'Z') {
                        timezone = TIMEZONE_UTC;
                        offset = offset6 + 1;
                    } else {
                        if (timezoneIndicator != '+') {
                            if (timezoneIndicator != '-') {
                                throw new IndexOutOfBoundsException("Invalid time zone indicator '" + timezoneIndicator + "'");
                            }
                        }
                        String timezoneOffset2 = date.substring(offset6);
                        if (timezoneOffset2.length() >= 5) {
                            timezoneOffset = timezoneOffset2;
                        } else {
                            timezoneOffset = timezoneOffset2 + "00";
                        }
                        int offset12 = offset6 + timezoneOffset.length();
                        if ("+0000".equals(timezoneOffset)) {
                            offset2 = offset12;
                        } else if ("+00:00".equals(timezoneOffset)) {
                            offset2 = offset12;
                        } else {
                            String timezoneId = "GMT" + timezoneOffset;
                            timezone = TimeZone.getTimeZone(timezoneId);
                            String act = timezone.getID();
                            if (!act.equals(timezoneId)) {
                                offset2 = offset12;
                                if (!act.replace(":", "").equals(timezoneId)) {
                                    throw new IndexOutOfBoundsException("Mismatching time zone indicator: " + timezoneId + " given, resolves to " + timezone.getID());
                                }
                            } else {
                                offset2 = offset12;
                            }
                            offset = offset2;
                        }
                        timezone = TIMEZONE_UTC;
                        offset = offset2;
                    }
                    Calendar calendar2 = new GregorianCalendar(timezone);
                    calendar2.setLenient(false);
                    calendar2.set(1, year);
                    calendar2.set(2, month - 1);
                    calendar2.set(5, day);
                    calendar2.set(11, hour);
                    calendar2.set(12, minutes);
                    calendar2.set(13, seconds);
                    calendar2.set(14, milliseconds);
                    pos.setIndex(offset);
                    return calendar2.getTime();
                }
                throw new IllegalArgumentException("No time zone indicator");
            } catch (IndexOutOfBoundsException e4) {
                e = e4;
                fail = e;
                if (date == null) {
                }
                msg = fail.getMessage();
                msg = "(" + fail.getClass().getName() + ")";
                ParseException ex222 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
                ex222.initCause(fail);
                throw ex222;
            } catch (NumberFormatException e5) {
                e = e5;
                fail = e;
                if (date == null) {
                }
                msg = fail.getMessage();
                msg = "(" + fail.getClass().getName() + ")";
                ParseException ex2222 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
                ex2222.initCause(fail);
                throw ex2222;
            } catch (IllegalArgumentException e6) {
                e = e6;
                fail = e;
                if (date == null) {
                }
                msg = fail.getMessage();
                msg = "(" + fail.getClass().getName() + ")";
                ParseException ex22222 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
                ex22222.initCause(fail);
                throw ex22222;
            }
        } catch (IndexOutOfBoundsException e7) {
            e = e7;
            fail = e;
            if (date == null) {
            }
            msg = fail.getMessage();
            msg = "(" + fail.getClass().getName() + ")";
            ParseException ex222222 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
            ex222222.initCause(fail);
            throw ex222222;
        } catch (NumberFormatException e8) {
            e = e8;
            fail = e;
            if (date == null) {
            }
            msg = fail.getMessage();
            msg = "(" + fail.getClass().getName() + ")";
            ParseException ex2222222 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
            ex2222222.initCause(fail);
            throw ex2222222;
        } catch (IllegalArgumentException e9) {
            e = e9;
            fail = e;
            if (date == null) {
            }
            msg = fail.getMessage();
            msg = "(" + fail.getClass().getName() + ")";
            ParseException ex22222222 = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
            ex22222222.initCause(fail);
            throw ex22222222;
        }
    }

    private static boolean checkOffset(String value, int offset, char expected) {
        return offset < value.length() && value.charAt(offset) == expected;
    }

    private static int parseInt(String value, int beginIndex, int endIndex) throws NumberFormatException {
        if (beginIndex < 0 || endIndex > value.length() || beginIndex > endIndex) {
            throw new NumberFormatException(value);
        }
        int digit = beginIndex;
        int result = 0;
        if (digit < endIndex) {
            int i = digit + 1;
            int digit2 = Character.digit(value.charAt(digit), 10);
            if (digit2 >= 0) {
                result = -digit2;
                digit = i;
            } else {
                throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
            }
        }
        while (digit < endIndex) {
            int i2 = digit + 1;
            int digit3 = Character.digit(value.charAt(digit), 10);
            if (digit3 >= 0) {
                result = (result * 10) - digit3;
                digit = i2;
            } else {
                throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
            }
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
        for (int i = offset; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c < '0' || c > '9') {
                return i;
            }
        }
        return string.length();
    }
}
