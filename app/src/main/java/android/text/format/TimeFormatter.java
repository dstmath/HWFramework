package android.text.format;

import android.content.res.Resources;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
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
                case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                case RILConstants.RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE /*45*/:
                case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                case RILConstants.RIL_REQUEST_CDMA_BROADCAST_ACTIVATION /*94*/:
                case RILConstants.RIL_REQUEST_CDMA_SUBSCRIPTION /*95*/:
                    modifier = currentChar;
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                    formatInternal("%a %b %e %H:%M:%S %Z %Y", wallTime, zoneInfo);
                    return false;
                case RILConstants.RIL_REQUEST_SET_BAND_MODE /*65*/:
                    str = (wallTime.getWeekDay() < 0 || wallTime.getWeekDay() >= DAYSPERWEEK) ? "?" : this.localeData.longWeekdayNames[wallTime.getWeekDay() + 1];
                    modifyAndAppend(str, modifier);
                    return false;
                case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                    if (modifier == 45) {
                        if (wallTime.getMonth() < 0 || wallTime.getMonth() >= MONSPERYEAR) {
                            str = "?";
                        } else {
                            str = this.localeData.longStandAloneMonthNames[wallTime.getMonth()];
                        }
                        modifyAndAppend(str, modifier);
                    } else {
                        str = (wallTime.getMonth() < 0 || wallTime.getMonth() >= MONSPERYEAR) ? "?" : this.localeData.longMonthNames[wallTime.getMonth()];
                        modifyAndAppend(str, modifier);
                    }
                    return false;
                case RILConstants.RIL_REQUEST_STK_GET_PROFILE /*67*/:
                    outputYear(wallTime.getYear(), true, false, modifier);
                    return false;
                case RILConstants.RIL_REQUEST_STK_SET_PROFILE /*68*/:
                    formatInternal("%m/%d/%y", wallTime, zoneInfo);
                    return false;
                case RILConstants.RIL_REQUEST_STK_SEND_ENVELOPE_COMMAND /*69*/:
                case RILConstants.RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE /*79*/:
                    break;
                case StatisticalConstant.TYPE_NAVIGATIONBAR_END /*70*/:
                    formatInternal("%Y-%m-%d", wallTime, zoneInfo);
                    return false;
                case StatisticalConstant.TYPE_SCREEN_SHOT /*71*/:
                case RILConstants.RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY /*86*/:
                case LogPower.WEBVIEW_PAUSED /*103*/:
                    int year = wallTime.getYear();
                    int yday = wallTime.getYearDay();
                    int wday = wallTime.getWeekDay();
                    while (true) {
                        int w;
                        int len = isLeap(year) ? DAYSPERLYEAR : DAYSPERNYEAR;
                        int bot = (((yday + 11) - wday) % DAYSPERWEEK) - 3;
                        int top = bot - (len % DAYSPERWEEK);
                        if (top < -3) {
                            top += DAYSPERWEEK;
                        }
                        if (yday >= top + len) {
                            year++;
                            w = 1;
                        } else if (yday >= bot) {
                            w = ((yday - bot) / DAYSPERWEEK) + 1;
                        } else {
                            year += FORCE_LOWER_CASE;
                            yday += isLeap(year) ? DAYSPERLYEAR : DAYSPERNYEAR;
                        }
                        if (currentChar == 'V') {
                            this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(w)});
                        } else if (currentChar == 'g') {
                            outputYear(year, false, true, modifier);
                        } else {
                            outputYear(year, true, true, modifier);
                        }
                        return false;
                    }
                case RILConstants.RIL_REQUEST_EXPLICIT_CALL_TRANSFER /*72*/:
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getHour())});
                    return false;
                case RILConstants.RIL_REQUEST_SET_PREFERRED_NETWORK_TYPE /*73*/:
                    int hour = wallTime.getHour() % MONSPERYEAR != 0 ? wallTime.getHour() % MONSPERYEAR : MONSPERYEAR;
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(hour)});
                    return false;
                case RILConstants.RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE /*77*/:
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMinute())});
                    return false;
                case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                    if (wallTime.getHour() >= MONSPERYEAR) {
                        str = this.localeData.amPm[1];
                    } else {
                        str = this.localeData.amPm[0];
                    }
                    modifyAndAppend(str, FORCE_LOWER_CASE);
                    return false;
                case StatisticalConstant.TYPE_TOUCH_FORCE_OPEAN_APPLICATION /*82*/:
                    formatInternal(DateUtils.HOUR_MINUTE_24, wallTime, zoneInfo);
                    return false;
                case RILConstants.RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE /*83*/:
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getSecond())});
                    return false;
                case RILConstants.RIL_REQUEST_CDMA_FLASH /*84*/:
                    formatInternal("%H:%M:%S", wallTime, zoneInfo);
                    return false;
                case RILConstants.RIL_REQUEST_CDMA_BURST_DTMF /*85*/:
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(((wallTime.getYearDay() + DAYSPERWEEK) - wallTime.getWeekDay()) / DAYSPERWEEK)});
                    return false;
                case RILConstants.RIL_REQUEST_CDMA_SEND_SMS /*87*/:
                    int weekDay;
                    int yearDay = wallTime.getYearDay() + DAYSPERWEEK;
                    if (wallTime.getWeekDay() != 0) {
                        weekDay = wallTime.getWeekDay() + FORCE_LOWER_CASE;
                    } else {
                        weekDay = 6;
                    }
                    int n = (yearDay - weekDay) / DAYSPERWEEK;
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(n)});
                    return false;
                case RILConstants.RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE /*88*/:
                    formatInternal(this.timeOnlyFormat, wallTime, zoneInfo);
                    return false;
                case RILConstants.RIL_REQUEST_GSM_GET_BROADCAST_CONFIG /*89*/:
                    outputYear(wallTime.getYear(), true, true, modifier);
                    return false;
                case StatisticalConstant.TYPE_TOUCH_FORCE_END /*90*/:
                    if (wallTime.getIsDst() < 0) {
                        return false;
                    }
                    modifyAndAppend(zoneInfo.getDisplayName(wallTime.getIsDst() != 0, 0), modifier);
                    return false;
                case RILConstants.RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM /*97*/:
                    str = (wallTime.getWeekDay() < 0 || wallTime.getWeekDay() >= DAYSPERWEEK) ? "?" : this.localeData.shortWeekdayNames[wallTime.getWeekDay() + 1];
                    modifyAndAppend(str, modifier);
                    return false;
                case RILConstants.RIL_REQUEST_DEVICE_IDENTITY /*98*/:
                case LogPower.WEBPAGE_STARTED /*104*/:
                    str = (wallTime.getMonth() < 0 || wallTime.getMonth() >= MONSPERYEAR) ? "?" : this.localeData.shortMonthNames[wallTime.getMonth()];
                    modifyAndAppend(str, modifier);
                    return false;
                case RILConstants.RIL_REQUEST_EXIT_EMERGENCY_CALLBACK_MODE /*99*/:
                    formatInternal(this.dateTimeFormat, wallTime, zoneInfo);
                    return false;
                case LogPower.ACTIVITY_RESUMED /*100*/:
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonthDay())});
                    return false;
                case LogPower.ACTIVITY_PAUSED /*101*/:
                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonthDay())});
                    return false;
                case LogPower.GAMEOF3D_RESUMED /*106*/:
                    int yearDay2 = wallTime.getYearDay() + 1;
                    this.numberFormatter.format(getFormat(modifier, "%03d", "%3d", "%d", "%03d"), new Object[]{Integer.valueOf(yearDay2)});
                    return false;
                case LogPower.GAMEOF3D_PAUSED /*107*/:
                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getHour())});
                    return false;
                case LogPower.APP_EXIT /*108*/:
                    int n2 = wallTime.getHour() % MONSPERYEAR != 0 ? wallTime.getHour() % MONSPERYEAR : MONSPERYEAR;
                    this.numberFormatter.format(getFormat(modifier, "%2d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(n2)});
                    return false;
                case LogPower.APP_LAUNCHER /*109*/:
                    this.numberFormatter.format(getFormat(modifier, "%02d", "%2d", "%d", "%02d"), new Object[]{Integer.valueOf(wallTime.getMonth() + 1)});
                    return false;
                case LogPower.ALL_DOWNLOAD_FINISH /*110*/:
                    this.outputBuilder.append('\n');
                    return false;
                case LogPower.APP_PROCESS_EXIT /*112*/:
                    if (wallTime.getHour() >= MONSPERYEAR) {
                        str = this.localeData.amPm[1];
                    } else {
                        str = this.localeData.amPm[0];
                    }
                    modifyAndAppend(str, modifier);
                    return false;
                case LogPower.APP_RUN_BG /*114*/:
                    formatInternal("%I:%M:%S %p", wallTime, zoneInfo);
                    return false;
                case LogPower.ALARM_BLOCKED /*115*/:
                    this.outputBuilder.append(Integer.toString(wallTime.mktime(zoneInfo)));
                    return false;
                case LogPower.SCREEN_OFF /*116*/:
                    this.outputBuilder.append('\t');
                    return false;
                case LogPower.KEYBOARD_SHOW /*117*/:
                    int day = wallTime.getWeekDay() == 0 ? DAYSPERWEEK : wallTime.getWeekDay();
                    this.numberFormatter.format("%d", new Object[]{Integer.valueOf(day)});
                    return false;
                case LogPower.KEYBOARD_HIDE /*118*/:
                    formatInternal("%e-%b-%Y", wallTime, zoneInfo);
                    return false;
                case LogPower.HW_PUSH_FINISH /*119*/:
                    this.numberFormatter.format("%d", new Object[]{Integer.valueOf(wallTime.getWeekDay())});
                    return false;
                case LogPower.FULL_SCREEN /*120*/:
                    formatInternal(this.dateOnlyFormat, wallTime, zoneInfo);
                    return false;
                case LogPower.ALARM_START /*121*/:
                    outputYear(wallTime.getYear(), false, true, modifier);
                    return false;
                case LogPower.NOTIFICATION_ENQUEUE /*122*/:
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
                    diff /= SECSPERMIN;
                    diff = ((diff / SECSPERMIN) * 100) + (diff % SECSPERMIN);
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
            case FORCE_LOWER_CASE /*-1*/:
                for (i = 0; i < str.length(); i++) {
                    this.outputBuilder.append(brokenToLower(str.charAt(i)));
                }
            case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                for (i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (brokenIsUpper(c)) {
                        c = brokenToLower(c);
                    } else if (brokenIsLower(c)) {
                        c = brokenToUpper(c);
                    }
                    this.outputBuilder.append(c);
                }
            case RILConstants.RIL_REQUEST_CDMA_BROADCAST_ACTIVATION /*94*/:
                for (i = 0; i < str.length(); i++) {
                    this.outputBuilder.append(brokenToUpper(str.charAt(i)));
                }
            default:
                this.outputBuilder.append(str);
        }
    }

    private void outputYear(int value, boolean outputTop, boolean outputBottom, int modifier) {
        int trail = value % 100;
        int lead = (value / 100) + (trail / 100);
        trail %= 100;
        if (trail < 0 && lead > 0) {
            trail += 100;
            lead += FORCE_LOWER_CASE;
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
            case RILConstants.RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE /*45*/:
                return dash;
            case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                return zero;
            case RILConstants.RIL_REQUEST_CDMA_SUBSCRIPTION /*95*/:
                return underscore;
            default:
                return normal;
        }
    }

    private static boolean isLeap(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION == 0);
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
