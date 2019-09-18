package android.text.format;

import android.content.res.Resources;
import java.nio.CharBuffer;
import java.util.Formatter;
import java.util.Locale;
import libcore.icu.LocaleData;
import libcore.util.ZoneInfo;

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
            if (sLocale == null || !locale.equals(sLocale)) {
                sLocale = locale;
                sLocaleData = LocaleData.get(locale);
                Resources r = Resources.getSystem();
                sTimeOnlyFormat = r.getString(17041245);
                sDateOnlyFormat = r.getString(17040537);
                sDateTimeFormat = r.getString(17039890);
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

    /* JADX WARNING: Code restructure failed: missing block: B:233:0x000a, code lost:
        continue;
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01e8, code lost:
        if (r21.getMonth() < 0) goto L_0x01fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01ee, code lost:
        if (r21.getMonth() < 12) goto L_0x01f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01f1, code lost:
        r7 = r0.localeData.shortMonthNames[r21.getMonth()];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x01fc, code lost:
        r7 = "?";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01fe, code lost:
        modifyAndAppend(r7, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0201, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0221, code lost:
        r5 = r6;
     */
    private boolean handleToken(CharBuffer formatBuffer, ZoneInfo.WallTime wallTime, ZoneInfo zoneInfo) {
        int currentChar;
        String str;
        int w;
        boolean z;
        String str2;
        int i;
        char sign;
        String str3;
        CharBuffer charBuffer = formatBuffer;
        ZoneInfo.WallTime wallTime2 = wallTime;
        ZoneInfo zoneInfo2 = zoneInfo;
        boolean z2 = false;
        int modifier = 0;
        while (true) {
            boolean isDst = true;
            if (formatBuffer.remaining() <= 1) {
                return true;
            }
            charBuffer.position(formatBuffer.position() + 1);
            currentChar = charBuffer.get(formatBuffer.position());
            int day = 7;
            int n2 = 12;
            switch (currentChar) {
                case 65:
                    modifyAndAppend((wallTime.getWeekDay() < 0 || wallTime.getWeekDay() >= 7) ? "?" : this.localeData.longWeekdayNames[wallTime.getWeekDay() + 1], modifier);
                    return false;
                case 66:
                    if (modifier == 45) {
                        if (wallTime.getMonth() < 0 || wallTime.getMonth() >= 12) {
                            str = "?";
                        } else {
                            str = this.localeData.longStandAloneMonthNames[wallTime.getMonth()];
                        }
                        modifyAndAppend(str, modifier);
                    } else {
                        modifyAndAppend((wallTime.getMonth() < 0 || wallTime.getMonth() >= 12) ? "?" : this.localeData.longMonthNames[wallTime.getMonth()], modifier);
                    }
                    return false;
                case 67:
                    outputYear(wallTime.getYear(), true, false, modifier);
                    return false;
                case 68:
                    formatInternal("%m/%d/%y", wallTime2, zoneInfo2);
                    return false;
                case 69:
                    break;
                case 70:
                    formatInternal("%Y-%m-%d", wallTime2, zoneInfo2);
                    return false;
                case 71:
                    break;
                case 72:
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getHour())});
                    return false;
                case 73:
                    if (wallTime.getHour() % 12 != 0) {
                        n2 = wallTime.getHour() % 12;
                    }
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(n2)});
                    return false;
                default:
                    switch (currentChar) {
                        case 79:
                            continue;
                        case 80:
                            if (wallTime.getHour() >= 12) {
                                str2 = this.localeData.amPm[1];
                            } else {
                                str2 = this.localeData.amPm[0];
                            }
                            modifyAndAppend(str2, -1);
                            return false;
                        default:
                            switch (currentChar) {
                                case 82:
                                    formatInternal(DateUtils.HOUR_MINUTE_24, wallTime2, zoneInfo2);
                                    return false;
                                case 83:
                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getSecond())});
                                    return false;
                                case 84:
                                    formatInternal("%H:%M:%S", wallTime2, zoneInfo2);
                                    return false;
                                case 85:
                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(((wallTime.getYearDay() + 7) - wallTime.getWeekDay()) / 7)});
                                    return false;
                                case 86:
                                    break;
                                case 87:
                                    int yearDay = wallTime.getYearDay() + 7;
                                    if (wallTime.getWeekDay() != 0) {
                                        i = wallTime.getWeekDay() - 1;
                                    } else {
                                        i = 6;
                                    }
                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf((yearDay - i) / 7)});
                                    return false;
                                case 88:
                                    formatInternal(this.timeOnlyFormat, wallTime2, zoneInfo2);
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
                                    modifyAndAppend(zoneInfo2.getDisplayName(isDst, 0), modifier);
                                    return false;
                                default:
                                    switch (currentChar) {
                                        case 94:
                                        case 95:
                                            break;
                                        default:
                                            switch (currentChar) {
                                                case 97:
                                                    modifyAndAppend((wallTime.getWeekDay() < 0 || wallTime.getWeekDay() >= 7) ? "?" : this.localeData.shortWeekdayNames[wallTime.getWeekDay() + 1], modifier);
                                                    return false;
                                                case 98:
                                                    break;
                                                case 99:
                                                    formatInternal(this.dateTimeFormat, wallTime2, zoneInfo2);
                                                    return false;
                                                case 100:
                                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonthDay())});
                                                    return false;
                                                case 101:
                                                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonthDay())});
                                                    return false;
                                                default:
                                                    switch (currentChar) {
                                                        case 103:
                                                            break;
                                                        case 104:
                                                            break;
                                                        default:
                                                            switch (currentChar) {
                                                                case 106:
                                                                    this.numberFormatter.format(getFormat(modifier, "%03d", "%3d", "%d", "%03d"), new Object[]{Integer.valueOf(wallTime.getYearDay() + 1)});
                                                                    return false;
                                                                case 107:
                                                                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getHour())});
                                                                    return false;
                                                                case 108:
                                                                    if (wallTime.getHour() % 12 != 0) {
                                                                        n2 = wallTime.getHour() % 12;
                                                                    }
                                                                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(n2)});
                                                                    return false;
                                                                case 109:
                                                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonth() + 1)});
                                                                    return false;
                                                                case 110:
                                                                    this.outputBuilder.append(10);
                                                                    return false;
                                                                default:
                                                                    switch (currentChar) {
                                                                        case 114:
                                                                            formatInternal("%I:%M:%S %p", wallTime2, zoneInfo2);
                                                                            return false;
                                                                        case 115:
                                                                            this.outputBuilder.append(Integer.toString(wallTime.mktime(zoneInfo)));
                                                                            return false;
                                                                        case 116:
                                                                            this.outputBuilder.append(9);
                                                                            return false;
                                                                        case 117:
                                                                            if (wallTime.getWeekDay() != 0) {
                                                                                day = wallTime.getWeekDay();
                                                                            }
                                                                            this.numberFormatter.format("%d", new Object[]{Integer.valueOf(day)});
                                                                            return false;
                                                                        case 118:
                                                                            formatInternal("%e-%b-%Y", wallTime2, zoneInfo2);
                                                                            return false;
                                                                        case 119:
                                                                            this.numberFormatter.format("%d", new Object[]{Integer.valueOf(wallTime.getWeekDay())});
                                                                            return false;
                                                                        case 120:
                                                                            formatInternal(this.dateOnlyFormat, wallTime2, zoneInfo2);
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
                                                                            this.numberFormatter.format(getFormat(modifier, "%04d", "%4d", "%d", "%04d"), new Object[]{Integer.valueOf(((diff2 / 60) * 100) + (diff2 % 60))});
                                                                            return false;
                                                                        default:
                                                                            switch (currentChar) {
                                                                                case 35:
                                                                                case 45:
                                                                                case 48:
                                                                                    break;
                                                                                case 43:
                                                                                    formatInternal("%a %b %e %H:%M:%S %Z %Y", wallTime2, zoneInfo2);
                                                                                    return false;
                                                                                case 77:
                                                                                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMinute())});
                                                                                    return false;
                                                                                case 112:
                                                                                    if (wallTime.getHour() >= 12) {
                                                                                        str3 = this.localeData.amPm[1];
                                                                                    } else {
                                                                                        str3 = this.localeData.amPm[0];
                                                                                    }
                                                                                    modifyAndAppend(str3, modifier);
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
            }
        }
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
                w = 1 + ((yday - bot) / 7);
            } else {
                year--;
                yday += isLeap(year) ? 366 : 365;
                z2 = false;
            }
        }
        if (currentChar == 86) {
            z = false;
            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(w)});
        } else {
            z = z2;
            if (currentChar == 103) {
                outputYear(year, z, true, modifier);
            } else {
                outputYear(year, true, true, modifier);
            }
        }
        return z;
    }

    private void modifyAndAppend(CharSequence str, int modifier) {
        int i = 0;
        if (modifier == -1) {
            while (true) {
                int i2 = i;
                if (i2 < str.length()) {
                    this.outputBuilder.append(brokenToLower(str.charAt(i2)));
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        } else if (modifier == 35) {
            while (true) {
                int i3 = i;
                if (i3 < str.length()) {
                    char c = str.charAt(i3);
                    if (brokenIsUpper(c)) {
                        c = brokenToLower(c);
                    } else if (brokenIsLower(c)) {
                        c = brokenToUpper(c);
                    }
                    this.outputBuilder.append(c);
                    i = i3 + 1;
                } else {
                    return;
                }
            }
        } else if (modifier != 94) {
            this.outputBuilder.append(str);
        } else {
            while (true) {
                int i4 = i;
                if (i4 < str.length()) {
                    this.outputBuilder.append(brokenToUpper(str.charAt(i4)));
                    i = i4 + 1;
                } else {
                    return;
                }
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
                this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(lead)});
            } else {
                this.outputBuilder.append("-0");
            }
        }
        if (outputBottom) {
            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(trail2 < 0 ? -trail2 : trail2)});
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
