package android.text.format;

import android.content.res.Resources;
import com.android.internal.R;
import java.nio.CharBuffer;
import java.util.Formatter;
import java.util.Locale;
import libcore.icu.LocaleData;
import libcore.util.ZoneInfo;
import libcore.util.ZoneInfo.WallTime;

class TimeFormatter {
    private static final int DAYSPERLYEAR = 366;
    private static final int DAYSPERNYEAR = 365;
    private static final int DAYSPERWEEK = 7;
    private static final int FORCE_LOWER_CASE = -1;
    private static final int HOURSPERDAY = 24;
    private static final int MINSPERHOUR = 60;
    private static final int MONSPERYEAR = 12;
    private static final int SECSPERMIN = 60;
    private static String sDateOnlyFormat;
    private static String sDateTimeFormat;
    private static Locale sLocale;
    private static LocaleData sLocaleData;
    private static String sTimeOnlyFormat;
    private final String dateOnlyFormat;
    private final String dateTimeFormat;
    private final LocaleData localeData;
    private Formatter numberFormatter;
    private StringBuilder outputBuilder;
    private final String timeOnlyFormat;

    public TimeFormatter() {
        synchronized (TimeFormatter.class) {
            Locale locale = Locale.getDefault();
            if (sLocale == null || (locale.equals(sLocale) ^ 1) != 0) {
                sLocale = locale;
                sLocaleData = LocaleData.get(locale);
                Resources r = Resources.getSystem();
                sTimeOnlyFormat = r.getString(R.string.time_of_day);
                sDateOnlyFormat = r.getString(R.string.month_day_year);
                sDateTimeFormat = r.getString(R.string.date_and_time);
            }
            this.dateTimeFormat = sDateTimeFormat;
            this.timeOnlyFormat = sTimeOnlyFormat;
            this.dateOnlyFormat = sDateOnlyFormat;
            this.localeData = sLocaleData;
        }
    }

    public String format(String pattern, WallTime wallTime, ZoneInfo zoneInfo) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            this.outputBuilder = stringBuilder;
            this.numberFormatter = new Formatter(stringBuilder, Locale.US);
            formatInternal(pattern, wallTime, zoneInfo);
            String result = stringBuilder.toString();
            if (this.localeData.zeroDigit != '0') {
                result = localizeDigits(result);
            }
            this.outputBuilder = null;
            this.numberFormatter = null;
            return result;
        } catch (Throwable th) {
            this.outputBuilder = null;
            this.numberFormatter = null;
        }
    }

    private String localizeDigits(String s) {
        int length = s.length();
        int offsetToLocalizedDigits = this.localeData.zeroDigit - 48;
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') {
                ch = (char) (ch + offsetToLocalizedDigits);
            }
            result.append(ch);
        }
        return result.toString();
    }

    private void formatInternal(String pattern, WallTime wallTime, ZoneInfo zoneInfo) {
        CharBuffer formatBuffer = CharBuffer.wrap(pattern);
        while (formatBuffer.remaining() > 0) {
            boolean outputCurrentChar = true;
            if (formatBuffer.get(formatBuffer.position()) == '%') {
                outputCurrentChar = handleToken(formatBuffer, wallTime, zoneInfo);
            }
            if (outputCurrentChar) {
                this.outputBuilder.append(formatBuffer.get(formatBuffer.position()));
            }
            formatBuffer.position(formatBuffer.position() + 1);
        }
    }

    private boolean handleToken(CharBuffer formatBuffer, WallTime wallTime, ZoneInfo zoneInfo) {
        int modifier = 0;
        while (formatBuffer.remaining() > 1) {
            formatBuffer.position(formatBuffer.position() + 1);
            char currentChar = formatBuffer.get(formatBuffer.position());
            String str;
            switch (currentChar) {
                case '#':
                case '-':
                case '0':
                case '^':
                case '_':
                    modifier = currentChar;
                    break;
                case '+':
                    formatInternal("%a %b %e %H:%M:%S %Z %Y", wallTime, zoneInfo);
                    return false;
                case 'A':
                    str = (wallTime.getWeekDay() < 0 || wallTime.getWeekDay() >= 7) ? "?" : this.localeData.longWeekdayNames[wallTime.getWeekDay() + 1];
                    modifyAndAppend(str, modifier);
                    return false;
                case 'B':
                    if (modifier == 45) {
                        if (wallTime.getMonth() < 0 || wallTime.getMonth() >= 12) {
                            str = "?";
                        } else {
                            str = this.localeData.longStandAloneMonthNames[wallTime.getMonth()];
                        }
                        modifyAndAppend(str, modifier);
                    } else {
                        str = (wallTime.getMonth() < 0 || wallTime.getMonth() >= 12) ? "?" : this.localeData.longMonthNames[wallTime.getMonth()];
                        modifyAndAppend(str, modifier);
                    }
                    return false;
                case 'C':
                    outputYear(wallTime.getYear(), true, false, modifier);
                    return false;
                case 'D':
                    formatInternal("%m/%d/%y", wallTime, zoneInfo);
                    return false;
                case 'E':
                case 'O':
                    break;
                case 'F':
                    formatInternal("%Y-%m-%d", wallTime, zoneInfo);
                    return false;
                case 'G':
                case 'V':
                case 'g':
                    int w;
                    int year = wallTime.getYear();
                    int yday = wallTime.getYearDay();
                    int wday = wallTime.getWeekDay();
                    while (true) {
                        int len = isLeap(year) ? 366 : 365;
                        int bot = (((yday + 11) - wday) % 7) - 3;
                        int top = bot - (len % 7);
                        if (top < -3) {
                            top += 7;
                        }
                        if (yday >= top + len) {
                            year++;
                            w = 1;
                        } else if (yday >= bot) {
                            w = ((yday - bot) / 7) + 1;
                        } else {
                            year--;
                            yday += isLeap(year) ? 366 : 365;
                        }
                    }
                    if (currentChar == 'V') {
                        this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(w)});
                    } else if (currentChar == 'g') {
                        outputYear(year, false, true, modifier);
                    } else {
                        outputYear(year, true, true, modifier);
                    }
                    return false;
                case 'H':
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getHour())});
                    return false;
                case 'I':
                    int hour = wallTime.getHour() % 12 != 0 ? wallTime.getHour() % 12 : 12;
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(hour)});
                    return false;
                case 'M':
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMinute())});
                    return false;
                case 'P':
                    if (wallTime.getHour() >= 12) {
                        str = this.localeData.amPm[1];
                    } else {
                        str = this.localeData.amPm[0];
                    }
                    modifyAndAppend(str, -1);
                    return false;
                case 'R':
                    formatInternal(DateUtils.HOUR_MINUTE_24, wallTime, zoneInfo);
                    return false;
                case 'S':
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getSecond())});
                    return false;
                case 'T':
                    formatInternal("%H:%M:%S", wallTime, zoneInfo);
                    return false;
                case 'U':
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(((wallTime.getYearDay() + 7) - wallTime.getWeekDay()) / 7)});
                    return false;
                case 'W':
                    int weekDay;
                    int yearDay = wallTime.getYearDay() + 7;
                    if (wallTime.getWeekDay() != 0) {
                        weekDay = wallTime.getWeekDay() - 1;
                    } else {
                        weekDay = 6;
                    }
                    int n = (yearDay - weekDay) / 7;
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(n)});
                    return false;
                case 'X':
                    formatInternal(this.timeOnlyFormat, wallTime, zoneInfo);
                    return false;
                case 'Y':
                    outputYear(wallTime.getYear(), true, true, modifier);
                    return false;
                case 'Z':
                    if (wallTime.getIsDst() < 0) {
                        return false;
                    }
                    modifyAndAppend(zoneInfo.getDisplayName(wallTime.getIsDst() != 0, 0), modifier);
                    return false;
                case 'a':
                    str = (wallTime.getWeekDay() < 0 || wallTime.getWeekDay() >= 7) ? "?" : this.localeData.shortWeekdayNames[wallTime.getWeekDay() + 1];
                    modifyAndAppend(str, modifier);
                    return false;
                case 'b':
                case 'h':
                    str = (wallTime.getMonth() < 0 || wallTime.getMonth() >= 12) ? "?" : this.localeData.shortMonthNames[wallTime.getMonth()];
                    modifyAndAppend(str, modifier);
                    return false;
                case 'c':
                    formatInternal(this.dateTimeFormat, wallTime, zoneInfo);
                    return false;
                case 'd':
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonthDay())});
                    return false;
                case 'e':
                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonthDay())});
                    return false;
                case 'j':
                    int yearDay2 = wallTime.getYearDay() + 1;
                    this.numberFormatter.format(getFormat(modifier, "%03d", "%3d", "%d", "%03d"), new Object[]{Integer.valueOf(yearDay2)});
                    return false;
                case 'k':
                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getHour())});
                    return false;
                case 'l':
                    int n2 = wallTime.getHour() % 12 != 0 ? wallTime.getHour() % 12 : 12;
                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(n2)});
                    return false;
                case 'm':
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonth() + 1)});
                    return false;
                case 'n':
                    this.outputBuilder.append(10);
                    return false;
                case 'p':
                    if (wallTime.getHour() >= 12) {
                        str = this.localeData.amPm[1];
                    } else {
                        str = this.localeData.amPm[0];
                    }
                    modifyAndAppend(str, modifier);
                    return false;
                case 'r':
                    formatInternal("%I:%M:%S %p", wallTime, zoneInfo);
                    return false;
                case 's':
                    this.outputBuilder.append(Integer.toString(wallTime.mktime(zoneInfo)));
                    return false;
                case 't':
                    this.outputBuilder.append(9);
                    return false;
                case 'u':
                    int day = wallTime.getWeekDay() == 0 ? 7 : wallTime.getWeekDay();
                    this.numberFormatter.format("%d", new Object[]{Integer.valueOf(day)});
                    return false;
                case 'v':
                    formatInternal("%e-%b-%Y", wallTime, zoneInfo);
                    return false;
                case 'w':
                    this.numberFormatter.format("%d", new Object[]{Integer.valueOf(wallTime.getWeekDay())});
                    return false;
                case 'x':
                    formatInternal(this.dateOnlyFormat, wallTime, zoneInfo);
                    return false;
                case 'y':
                    outputYear(wallTime.getYear(), false, true, modifier);
                    return false;
                case 'z':
                    if (wallTime.getIsDst() < 0) {
                        return false;
                    }
                    char sign;
                    int diff = wallTime.getGmtOffset();
                    if (diff < 0) {
                        sign = '-';
                        diff = -diff;
                    } else {
                        sign = '+';
                    }
                    this.outputBuilder.append(sign);
                    diff /= 60;
                    diff = ((diff / 60) * 100) + (diff % 60);
                    this.numberFormatter.format(getFormat(modifier, "%04d", "%4d", "%d", "%04d"), new Object[]{Integer.valueOf(diff)});
                    return false;
                default:
                    return true;
            }
        }
        return true;
    }

    private void modifyAndAppend(CharSequence str, int modifier) {
        int i;
        switch (modifier) {
            case -1:
                for (i = 0; i < str.length(); i++) {
                    this.outputBuilder.append(brokenToLower(str.charAt(i)));
                }
                return;
            case 35:
                for (i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (brokenIsUpper(c)) {
                        c = brokenToLower(c);
                    } else if (brokenIsLower(c)) {
                        c = brokenToUpper(c);
                    }
                    this.outputBuilder.append(c);
                }
                return;
            case 94:
                for (i = 0; i < str.length(); i++) {
                    this.outputBuilder.append(brokenToUpper(str.charAt(i)));
                }
                return;
            default:
                this.outputBuilder.append(str);
                return;
        }
    }

    private void outputYear(int value, boolean outputTop, boolean outputBottom, int modifier) {
        int trail = value % 100;
        int lead = (value / 100) + (trail / 100);
        trail %= 100;
        if (trail < 0 && lead > 0) {
            trail += 100;
            lead--;
        } else if (lead < 0 && trail > 0) {
            trail -= 100;
            lead++;
        }
        if (outputTop) {
            if (lead != 0 || trail >= 0) {
                this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(lead)});
            } else {
                this.outputBuilder.append("-0");
            }
        }
        if (outputBottom) {
            int n = trail < 0 ? -trail : trail;
            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(n)});
        }
    }

    private static String getFormat(int modifier, String normal, String underscore, String dash, String zero) {
        switch (modifier) {
            case 45:
                return dash;
            case 48:
                return zero;
            case 95:
                return underscore;
            default:
                return normal;
        }
    }

    private static boolean isLeap(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    private static boolean brokenIsUpper(char toCheck) {
        return toCheck >= DateFormat.CAPITAL_AM_PM && toCheck <= 'Z';
    }

    private static boolean brokenIsLower(char toCheck) {
        return toCheck >= DateFormat.AM_PM && toCheck <= DateFormat.TIME_ZONE;
    }

    private static char brokenToLower(char input) {
        if (input < DateFormat.CAPITAL_AM_PM || input > 'Z') {
            return input;
        }
        return (char) ((input - 65) + 97);
    }

    private static char brokenToUpper(char input) {
        if (input < DateFormat.AM_PM || input > DateFormat.TIME_ZONE) {
            return input;
        }
        return (char) ((input - 97) + 65);
    }
}
