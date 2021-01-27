package android.text.format;

import android.content.res.Resources;
import com.android.internal.R;
import java.nio.CharBuffer;
import java.util.Formatter;
import java.util.Locale;
import libcore.icu.LocaleData;
import libcore.util.ZoneInfo;

/* access modifiers changed from: package-private */
public class TimeFormatter {
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
            if (sLocale == null || !locale.equals(sLocale)) {
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

    public String format(String pattern, ZoneInfo.WallTime wallTime, ZoneInfo zoneInfo) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            this.outputBuilder = stringBuilder;
            this.numberFormatter = new Formatter(stringBuilder, Locale.US);
            formatInternal(pattern, wallTime, zoneInfo);
            String result = stringBuilder.toString();
            if (this.localeData.zeroDigit != '0') {
                result = localizeDigits(result);
            }
            return result;
        } finally {
            this.outputBuilder = null;
            this.numberFormatter = null;
        }
    }

    private String localizeDigits(String s) {
        int length = s.length();
        int offsetToLocalizedDigits = this.localeData.zeroDigit - '0';
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

    private void formatInternal(String pattern, ZoneInfo.WallTime wallTime, ZoneInfo zoneInfo) {
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

    /* JADX DEBUG: Multi-variable search result rejected for r14v2, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r14v1 */
    /* JADX WARN: Type inference failed for: r14v3 */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x02fc, code lost:
        if (r23.getMonth() < 0) goto L_0x0311;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0302, code lost:
        if (r23.getMonth() < 12) goto L_0x0305;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0305, code lost:
        r16 = r21.localeData.shortMonthNames[r23.getMonth()];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0311, code lost:
        modifyAndAppend(r16, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0316, code lost:
        return false;
     */
    private boolean handleToken(CharBuffer formatBuffer, ZoneInfo.WallTime wallTime, ZoneInfo zoneInfo) {
        int currentChar;
        boolean z;
        String str;
        boolean z2;
        String str2;
        int w;
        int i;
        String str3;
        int i2;
        char sign;
        int modifier = 0;
        while (true) {
            boolean isDst = true;
            if (formatBuffer.remaining() <= 1) {
                return true;
            }
            formatBuffer.position(formatBuffer.position() + 1);
            currentChar = formatBuffer.get(formatBuffer.position());
            if (currentChar != 35) {
                if (currentChar == 43) {
                    formatInternal("%a %b %e %H:%M:%S %Z %Y", wallTime, zoneInfo);
                    return false;
                } else if (!(currentChar == 45 || currentChar == 48)) {
                    if (currentChar != 77) {
                        int n2 = 12;
                        if (currentChar == 112) {
                            if (wallTime.getHour() >= 12) {
                                str = this.localeData.amPm[1];
                                z = false;
                            } else {
                                z = false;
                                str = this.localeData.amPm[0];
                            }
                            modifyAndAppend(str, modifier);
                            return z;
                        } else if (currentChar == 79) {
                            continue;
                        } else if (currentChar == 80) {
                            if (wallTime.getHour() >= 12) {
                                str2 = this.localeData.amPm[1];
                                z2 = false;
                            } else {
                                z2 = false;
                                str2 = this.localeData.amPm[0];
                            }
                            modifyAndAppend(str2, -1);
                            return z2;
                        } else if (!(currentChar == 94 || currentChar == 95)) {
                            int day = 7;
                            if (currentChar != 103) {
                                String str4 = "?";
                                if (currentChar != 104) {
                                    switch (currentChar) {
                                        case 65:
                                            if (wallTime.getWeekDay() >= 0 && wallTime.getWeekDay() < 7) {
                                                str4 = this.localeData.longWeekdayNames[wallTime.getWeekDay() + 1];
                                            }
                                            modifyAndAppend(str4, modifier);
                                            return false;
                                        case 66:
                                            if (modifier == 45) {
                                                if (wallTime.getMonth() < 0 || wallTime.getMonth() >= 12) {
                                                    str3 = str4;
                                                } else {
                                                    str3 = this.localeData.longStandAloneMonthNames[wallTime.getMonth()];
                                                }
                                                modifyAndAppend(str3, modifier);
                                            } else {
                                                if (wallTime.getMonth() >= 0 && wallTime.getMonth() < 12) {
                                                    str4 = this.localeData.longMonthNames[wallTime.getMonth()];
                                                }
                                                modifyAndAppend(str4, modifier);
                                            }
                                            return false;
                                        case 67:
                                            outputYear(wallTime.getYear(), true, false, modifier);
                                            return false;
                                        case 68:
                                            formatInternal("%m/%d/%y", wallTime, zoneInfo);
                                            return false;
                                        case 69:
                                            break;
                                        case 70:
                                            formatInternal("%Y-%m-%d", wallTime, zoneInfo);
                                            return false;
                                        case 71:
                                            break;
                                        case 72:
                                            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(wallTime.getHour()));
                                            return false;
                                        case 73:
                                            if (wallTime.getHour() % 12 != 0) {
                                                n2 = wallTime.getHour() % 12;
                                            }
                                            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(n2));
                                            return false;
                                        default:
                                            switch (currentChar) {
                                                case 82:
                                                    formatInternal(DateUtils.HOUR_MINUTE_24, wallTime, zoneInfo);
                                                    return false;
                                                case 83:
                                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(wallTime.getSecond()));
                                                    return false;
                                                case 84:
                                                    formatInternal("%H:%M:%S", wallTime, zoneInfo);
                                                    return false;
                                                case 85:
                                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(((wallTime.getYearDay() + 7) - wallTime.getWeekDay()) / 7));
                                                    return false;
                                                case 86:
                                                    break;
                                                case 87:
                                                    int yearDay = wallTime.getYearDay() + 7;
                                                    if (wallTime.getWeekDay() != 0) {
                                                        i2 = wallTime.getWeekDay() - 1;
                                                    } else {
                                                        i2 = 6;
                                                    }
                                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf((yearDay - i2) / 7));
                                                    return false;
                                                case 88:
                                                    formatInternal(this.timeOnlyFormat, wallTime, zoneInfo);
                                                    return false;
                                                case 89:
                                                    outputYear(wallTime.getYear(), true, true, modifier);
                                                    return false;
                                                case 90:
                                                    if (wallTime.getIsDst() < 0) {
                                                        return false;
                                                    }
                                                    if (wallTime.getIsDst() == 0) {
                                                        isDst = false;
                                                    }
                                                    modifyAndAppend(zoneInfo.getDisplayName(isDst, 0), modifier);
                                                    return false;
                                                default:
                                                    switch (currentChar) {
                                                        case 97:
                                                            if (wallTime.getWeekDay() >= 0 && wallTime.getWeekDay() < 7) {
                                                                str4 = this.localeData.shortWeekdayNames[wallTime.getWeekDay() + 1];
                                                            }
                                                            modifyAndAppend(str4, modifier);
                                                            return false;
                                                        case 98:
                                                            break;
                                                        case 99:
                                                            formatInternal(this.dateTimeFormat, wallTime, zoneInfo);
                                                            return false;
                                                        case 100:
                                                            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(wallTime.getMonthDay()));
                                                            return false;
                                                        case 101:
                                                            this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), Integer.valueOf(wallTime.getMonthDay()));
                                                            return false;
                                                        default:
                                                            switch (currentChar) {
                                                                case 106:
                                                                    this.numberFormatter.format(getFormat(modifier, "%03d", "%3d", "%d", "%03d"), Integer.valueOf(wallTime.getYearDay() + 1));
                                                                    return false;
                                                                case 107:
                                                                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), Integer.valueOf(wallTime.getHour()));
                                                                    return false;
                                                                case 108:
                                                                    if (wallTime.getHour() % 12 != 0) {
                                                                        n2 = wallTime.getHour() % 12;
                                                                    }
                                                                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), Integer.valueOf(n2));
                                                                    return false;
                                                                case 109:
                                                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(wallTime.getMonth() + 1));
                                                                    return false;
                                                                case 110:
                                                                    this.outputBuilder.append('\n');
                                                                    return false;
                                                                default:
                                                                    switch (currentChar) {
                                                                        case 114:
                                                                            formatInternal("%I:%M:%S %p", wallTime, zoneInfo);
                                                                            return false;
                                                                        case 115:
                                                                            this.outputBuilder.append(Integer.toString(wallTime.mktime(zoneInfo)));
                                                                            return false;
                                                                        case 116:
                                                                            this.outputBuilder.append('\t');
                                                                            return false;
                                                                        case 117:
                                                                            if (wallTime.getWeekDay() != 0) {
                                                                                day = wallTime.getWeekDay();
                                                                            }
                                                                            this.numberFormatter.format("%d", Integer.valueOf(day));
                                                                            return false;
                                                                        case 118:
                                                                            formatInternal("%e-%b-%Y", wallTime, zoneInfo);
                                                                            return false;
                                                                        case 119:
                                                                            this.numberFormatter.format("%d", Integer.valueOf(wallTime.getWeekDay()));
                                                                            return false;
                                                                        case 120:
                                                                            formatInternal(this.dateOnlyFormat, wallTime, zoneInfo);
                                                                            return false;
                                                                        case 121:
                                                                            outputYear(wallTime.getYear(), false, true, modifier);
                                                                            return false;
                                                                        case 122:
                                                                            if (wallTime.getIsDst() < 0) {
                                                                                return false;
                                                                            }
                                                                            int diff = wallTime.getGmtOffset();
                                                                            if (diff < 0) {
                                                                                sign = '-';
                                                                                diff = -diff;
                                                                            } else {
                                                                                sign = '+';
                                                                            }
                                                                            this.outputBuilder.append(sign);
                                                                            int diff2 = diff / 60;
                                                                            this.numberFormatter.format(getFormat(modifier, "%04d", "%4d", "%d", "%04d"), Integer.valueOf(((diff2 / 60) * 100) + (diff2 % 60)));
                                                                            return false;
                                                                        default:
                                                                            return true;
                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                                }
                            }
                        }
                    } else {
                        this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(wallTime.getMinute()));
                        return false;
                    }
                }
            }
            modifier = currentChar;
        }
        int year = wallTime.getYear();
        int yday = wallTime.getYearDay();
        int wday = wallTime.getWeekDay();
        while (true) {
            int i3 = 366;
            int len = isLeap(year) ? 366 : 365;
            int bot = (((yday + 11) - wday) % 7) - 3;
            int top = bot - (len % 7);
            if (top < -3) {
                top += 7;
            }
            if (yday >= top + len) {
                year++;
                w = 1;
                i = 1;
            } else if (yday >= bot) {
                i = 1;
                w = ((yday - bot) / 7) + 1;
            } else {
                year--;
                if (!isLeap(year)) {
                    i3 = 365;
                }
                yday += i3;
            }
        }
        if (currentChar == 86) {
            Formatter formatter = this.numberFormatter;
            String format = getFormat(modifier, "%02d", "%2d", "%d", "%02d");
            Object[] objArr = new Object[i];
            objArr[0] = Integer.valueOf(w);
            formatter.format(format, objArr);
            return false;
        } else if (currentChar == 103) {
            outputYear(year, false, i, modifier);
            return false;
        } else {
            outputYear(year, i, i, modifier);
            return false;
        }
    }

    private void modifyAndAppend(CharSequence str, int modifier) {
        if (modifier == -1) {
            for (int i = 0; i < str.length(); i++) {
                this.outputBuilder.append(brokenToLower(str.charAt(i)));
            }
        } else if (modifier == 35) {
            for (int i2 = 0; i2 < str.length(); i2++) {
                char c = str.charAt(i2);
                if (brokenIsUpper(c)) {
                    c = brokenToLower(c);
                } else if (brokenIsLower(c)) {
                    c = brokenToUpper(c);
                }
                this.outputBuilder.append(c);
            }
        } else if (modifier != 94) {
            this.outputBuilder.append(str);
        } else {
            for (int i3 = 0; i3 < str.length(); i3++) {
                this.outputBuilder.append(brokenToUpper(str.charAt(i3)));
            }
        }
    }

    private void outputYear(int value, boolean outputTop, boolean outputBottom, int modifier) {
        int trail = value % 100;
        int lead = (value / 100) + (trail / 100);
        int trail2 = trail % 100;
        if (trail2 < 0 && lead > 0) {
            trail2 += 100;
            lead--;
        } else if (lead < 0 && trail2 > 0) {
            trail2 -= 100;
            lead++;
        }
        if (outputTop) {
            if (lead != 0 || trail2 >= 0) {
                this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(lead));
            } else {
                this.outputBuilder.append("-0");
            }
        }
        if (outputBottom) {
            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), Integer.valueOf(trail2 < 0 ? -trail2 : trail2));
        }
    }

    private static String getFormat(int modifier, String normal, String underscore, String dash, String zero) {
        if (modifier == 45) {
            return dash;
        }
        if (modifier == 48) {
            return zero;
        }
        if (modifier != 95) {
            return normal;
        }
        return underscore;
    }

    private static boolean isLeap(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    private static boolean brokenIsUpper(char toCheck) {
        return toCheck >= 'A' && toCheck <= 'Z';
    }

    private static boolean brokenIsLower(char toCheck) {
        return toCheck >= 'a' && toCheck <= 'z';
    }

    private static char brokenToLower(char input) {
        if (input < 'A' || input > 'Z') {
            return input;
        }
        return (char) ((input - 'A') + 97);
    }

    private static char brokenToUpper(char input) {
        if (input < 'a' || input > 'z') {
            return input;
        }
        return (char) ((input - 'a') + 65);
    }
}
