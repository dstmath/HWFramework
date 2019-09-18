package android.net.http;

import android.text.format.Time;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.LangUtils;

final class LegacyHttpDateTime {
    private static final Pattern HTTP_DATE_ANSIC_PATTERN = Pattern.compile(HTTP_DATE_ANSIC_REGEXP);
    private static final String HTTP_DATE_ANSIC_REGEXP = "[ ]([A-Za-z]{3,9})[ ]+([0-9]{1,2})[ ]([0-9]{1,2}:[0-9][0-9]:[0-9][0-9])[ ]([0-9]{2,4})";
    private static final Pattern HTTP_DATE_RFC_PATTERN = Pattern.compile(HTTP_DATE_RFC_REGEXP);
    private static final String HTTP_DATE_RFC_REGEXP = "([0-9]{1,2})[- ]([A-Za-z]{3,9})[- ]([0-9]{2,4})[ ]([0-9]{1,2}:[0-9][0-9]:[0-9][0-9])";

    private static class TimeOfDay {
        int hour;
        int minute;
        int second;

        TimeOfDay(int h, int m, int s) {
            this.hour = h;
            this.minute = m;
            this.second = s;
        }
    }

    LegacyHttpDateTime() {
    }

    public static long parse(String timeString) throws IllegalArgumentException {
        TimeOfDay timeOfDay;
        int year;
        int month;
        int date;
        Matcher rfcMatcher = HTTP_DATE_RFC_PATTERN.matcher(timeString);
        if (rfcMatcher.find()) {
            date = getDate(rfcMatcher.group(1));
            month = getMonth(rfcMatcher.group(2));
            year = getYear(rfcMatcher.group(3));
            timeOfDay = getTime(rfcMatcher.group(4));
        } else {
            Matcher ansicMatcher = HTTP_DATE_ANSIC_PATTERN.matcher(timeString);
            if (ansicMatcher.find()) {
                month = getMonth(ansicMatcher.group(1));
                date = getDate(ansicMatcher.group(2));
                TimeOfDay timeOfDay2 = getTime(ansicMatcher.group(3));
                year = getYear(ansicMatcher.group(4));
                timeOfDay = timeOfDay2;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (year >= 2038) {
            year = 2038;
            month = 0;
            date = 1;
        }
        Time time = new Time("UTC");
        time.set(timeOfDay.second, timeOfDay.minute, timeOfDay.hour, date, month, year);
        return time.toMillis(false);
    }

    private static int getDate(String dateString) {
        if (dateString.length() == 2) {
            return ((dateString.charAt(0) - '0') * 10) + (dateString.charAt(1) - '0');
        }
        return dateString.charAt(0) - '0';
    }

    private static int getMonth(String monthString) {
        int hash = ((Character.toLowerCase(monthString.charAt(0)) + Character.toLowerCase(monthString.charAt(1))) + Character.toLowerCase(monthString.charAt(2))) - 291;
        if (hash == 22) {
            return 0;
        }
        if (hash == 26) {
            return 7;
        }
        if (hash == 29) {
            return 2;
        }
        if (hash == 32) {
            return 3;
        }
        if (hash == 40) {
            return 6;
        }
        if (hash == 42) {
            return 5;
        }
        if (hash == 48) {
            return 10;
        }
        switch (hash) {
            case HTTP.HT /*9*/:
                return 11;
            case HTTP.LF /*10*/:
                return 1;
            default:
                switch (hash) {
                    case 35:
                        return 9;
                    case 36:
                        return 4;
                    case LangUtils.HASH_OFFSET /*37*/:
                        return 8;
                    default:
                        throw new IllegalArgumentException();
                }
        }
    }

    private static int getYear(String yearString) {
        if (yearString.length() == 2) {
            int year = ((yearString.charAt(0) - '0') * 10) + (yearString.charAt(1) - '0');
            if (year >= 70) {
                return year + 1900;
            }
            return year + 2000;
        } else if (yearString.length() == 3) {
            return ((yearString.charAt(0) - '0') * 100) + ((yearString.charAt(1) - '0') * 10) + (yearString.charAt(2) - '0') + 1900;
        } else {
            if (yearString.length() == 4) {
                return ((yearString.charAt(0) - '0') * 1000) + ((yearString.charAt(1) - '0') * 100) + ((yearString.charAt(2) - '0') * 10) + (yearString.charAt(3) - '0');
            }
            return 1970;
        }
    }

    private static TimeOfDay getTime(String timeString) {
        int i;
        int i2 = 0 + 1;
        int hour = timeString.charAt(0) - 48;
        if (timeString.charAt(i2) != ':') {
            hour = (hour * 10) + (timeString.charAt(i2) - 48);
            i2++;
        }
        int i3 = i + 1 + 1 + 1 + 1;
        return new TimeOfDay(hour, ((timeString.charAt(i2 + 1) - 48) * 10) + (timeString.charAt((i2 + 1) + 1) - 48), ((timeString.charAt(i) - 48) * 10) + (timeString.charAt(i) - 48));
    }
}
