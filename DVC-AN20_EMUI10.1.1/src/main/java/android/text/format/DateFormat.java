package android.text.format;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.wifi.WifiScanLog;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

public class DateFormat {
    @Deprecated
    public static final char AM_PM = 'a';
    @Deprecated
    public static final char CAPITAL_AM_PM = 'A';
    @Deprecated
    public static final char DATE = 'd';
    @Deprecated
    public static final char DAY = 'E';
    @Deprecated
    public static final char HOUR = 'h';
    @Deprecated
    public static final char HOUR_OF_DAY = 'k';
    @Deprecated
    public static final char MINUTE = 'm';
    @Deprecated
    public static final char MONTH = 'M';
    @Deprecated
    public static final char QUOTE = '\'';
    @Deprecated
    public static final char SECONDS = 's';
    @Deprecated
    public static final char STANDALONE_MONTH = 'L';
    @Deprecated
    public static final char TIME_ZONE = 'z';
    @Deprecated
    public static final char YEAR = 'y';
    private static boolean sIs24Hour;
    private static Locale sIs24HourLocale;
    private static final Object sLocaleLock = new Object();

    public static boolean is24HourFormat(Context context) {
        return is24HourFormat(context, context.getUserId());
    }

    @UnsupportedAppUsage
    public static boolean is24HourFormat(Context context, int userHandle) {
        String value = Settings.System.getStringForUser(context.getContentResolver(), Settings.System.TIME_12_24, userHandle);
        if (value != null) {
            return value.equals(WifiScanLog.EVENT_KEY24);
        }
        return is24HourLocale(context.getResources().getConfiguration().locale);
    }

    /* JADX INFO: Multiple debug info for r0v7 boolean: [D('is24Hour' boolean), D('sdf' java.text.SimpleDateFormat)] */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        r1 = java.text.DateFormat.getTimeInstance(1, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        if ((r1 instanceof java.text.SimpleDateFormat) == false) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r2 = hasDesignator(((java.text.SimpleDateFormat) r1).toPattern(), 'H');
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
        r3 = android.text.format.DateFormat.sLocaleLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        android.text.format.DateFormat.sIs24HourLocale = r4;
        android.text.format.DateFormat.sIs24Hour = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0036, code lost:
        return r2;
     */
    public static boolean is24HourLocale(Locale locale) {
        synchronized (sLocaleLock) {
            if (sIs24HourLocale != null && sIs24HourLocale.equals(locale)) {
                return sIs24Hour;
            }
        }
    }

    public static String getBestDateTimePattern(Locale locale, String skeleton) {
        return ICU.getBestDateTimePattern(skeleton, locale);
    }

    public static java.text.DateFormat getTimeFormat(Context context) {
        return new SimpleDateFormat(getTimeFormatString(context), context.getResources().getConfiguration().locale);
    }

    @UnsupportedAppUsage
    public static String getTimeFormatString(Context context) {
        return getTimeFormatString(context, context.getUserId());
    }

    @UnsupportedAppUsage
    public static String getTimeFormatString(Context context, int userHandle) {
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        return is24HourFormat(context, userHandle) ? d.timeFormat_Hm : d.timeFormat_hm;
    }

    public static java.text.DateFormat getDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(3, context.getResources().getConfiguration().locale);
    }

    public static java.text.DateFormat getLongDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(1, context.getResources().getConfiguration().locale);
    }

    public static java.text.DateFormat getMediumDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(2, context.getResources().getConfiguration().locale);
    }

    public static char[] getDateFormatOrder(Context context) {
        return ICU.getDateFormatOrder(getDateFormatString(context));
    }

    private static String getDateFormatString(Context context) {
        java.text.DateFormat df = java.text.DateFormat.getDateInstance(3, context.getResources().getConfiguration().locale);
        if (df instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) df).toPattern();
        }
        throw new AssertionError("!(df instanceof SimpleDateFormat)");
    }

    public static CharSequence format(CharSequence inFormat, long inTimeInMillis) {
        return format(inFormat, new Date(inTimeInMillis));
    }

    public static CharSequence format(CharSequence inFormat, Date inDate) {
        Calendar c = new GregorianCalendar();
        c.setTime(inDate);
        return format(inFormat, c);
    }

    @UnsupportedAppUsage
    public static boolean hasSeconds(CharSequence inFormat) {
        return hasDesignator(inFormat, 's');
    }

    @UnsupportedAppUsage
    public static boolean hasDesignator(CharSequence inFormat, char designator) {
        if (inFormat == null) {
            return false;
        }
        int length = inFormat.length();
        boolean insideQuote = false;
        for (int i = 0; i < length; i++) {
            char c = inFormat.charAt(i);
            boolean z = true;
            if (c == '\'') {
                if (insideQuote) {
                    z = false;
                }
                insideQuote = z;
            } else if (!insideQuote && c == designator) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00da  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00e7 A[SYNTHETIC] */
    public static CharSequence format(CharSequence inFormat, Calendar inDate) {
        String replacement;
        SpannableStringBuilder s = new SpannableStringBuilder(inFormat);
        LocaleData localeData = LocaleData.get(Locale.getDefault());
        int len = inFormat.length();
        int i = 0;
        while (i < len) {
            int count = 1;
            int c = s.charAt(i);
            if (c == 39) {
                count = appendQuotedText(s, i);
                len = s.length();
            } else {
                while (i + count < len && s.charAt(i + count) == c) {
                    count++;
                }
                if (c != 65) {
                    if (c != 69) {
                        if (c != 72) {
                            if (c != 97) {
                                if (c != 104) {
                                    if (c != 107) {
                                        if (c == 109) {
                                            replacement = zeroPad(inDate.get(12), count);
                                        } else if (c == 115) {
                                            replacement = zeroPad(inDate.get(13), count);
                                        } else if (c != 99) {
                                            if (c == 100) {
                                                replacement = zeroPad(inDate.get(5), count);
                                            } else if (c == 121) {
                                                replacement = getYearString(inDate.get(1), count);
                                            } else if (c != 122) {
                                                switch (c) {
                                                    case 75:
                                                        break;
                                                    case 76:
                                                    case 77:
                                                        replacement = getMonthString(localeData, inDate.get(2), count, c);
                                                        break;
                                                    default:
                                                        replacement = null;
                                                        break;
                                                }
                                            } else {
                                                replacement = getTimeZoneString(inDate, count);
                                            }
                                        }
                                        if (replacement != null) {
                                            s.replace(i, i + count, (CharSequence) replacement);
                                            count = replacement.length();
                                            len = s.length();
                                        }
                                    }
                                }
                                int hour = inDate.get(10);
                                if (c == 104 && hour == 0) {
                                    hour = 12;
                                }
                                replacement = zeroPad(hour, count);
                                if (replacement != null) {
                                }
                            }
                        }
                        replacement = zeroPad(inDate.get(11), count);
                        if (replacement != null) {
                        }
                    }
                    replacement = getDayOfWeekString(localeData, inDate.get(7), count, c);
                    if (replacement != null) {
                    }
                }
                replacement = localeData.amPm[inDate.get(9) + 0];
                if (replacement != null) {
                }
            }
            i += count;
        }
        if (inFormat instanceof Spanned) {
            return new SpannedString(s);
        }
        return s.toString();
    }

    private static String getDayOfWeekString(LocaleData ld, int day, int count, int kind) {
        boolean standalone = kind == 99;
        return count == 5 ? standalone ? ld.tinyStandAloneWeekdayNames[day] : ld.tinyWeekdayNames[day] : count == 4 ? standalone ? ld.longStandAloneWeekdayNames[day] : ld.longWeekdayNames[day] : standalone ? ld.shortStandAloneWeekdayNames[day] : ld.shortWeekdayNames[day];
    }

    private static String getMonthString(LocaleData ld, int month, int count, int kind) {
        boolean standalone = kind == 76;
        if (count == 5) {
            return standalone ? ld.tinyStandAloneMonthNames[month] : ld.tinyMonthNames[month];
        }
        if (count == 4) {
            return standalone ? ld.longStandAloneMonthNames[month] : ld.longMonthNames[month];
        }
        if (count == 3) {
            return standalone ? ld.shortStandAloneMonthNames[month] : ld.shortMonthNames[month];
        }
        return zeroPad(month + 1, count);
    }

    private static String getTimeZoneString(Calendar inDate, int count) {
        TimeZone tz = inDate.getTimeZone();
        if (count < 2) {
            return formatZoneOffset(inDate.get(16) + inDate.get(15), count);
        }
        return tz.getDisplayName(inDate.get(16) != 0, 0);
    }

    private static String formatZoneOffset(int offset, int count) {
        int offset2 = offset / 1000;
        StringBuilder tb = new StringBuilder();
        if (offset2 < 0) {
            tb.insert(0, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            offset2 = -offset2;
        } else {
            tb.insert(0, HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
        }
        tb.append(zeroPad(offset2 / 3600, 2));
        tb.append(zeroPad((offset2 % 3600) / 60, 2));
        return tb.toString();
    }

    private static String getYearString(int year, int count) {
        if (count <= 2) {
            return zeroPad(year % 100, 2);
        }
        return String.format(Locale.getDefault(), "%d", Integer.valueOf(year));
    }

    public static int appendQuotedText(SpannableStringBuilder formatString, int index) {
        int length = formatString.length();
        if (index + 1 >= length || formatString.charAt(index + 1) != '\'') {
            int count = 0;
            formatString.delete(index, index + 1);
            int length2 = length - 1;
            while (true) {
                if (index >= length2) {
                    break;
                } else if (formatString.charAt(index) != '\'') {
                    index++;
                    count++;
                } else if (index + 1 >= length2 || formatString.charAt(index + 1) != '\'') {
                    formatString.delete(index, index + 1);
                } else {
                    formatString.delete(index, index + 1);
                    length2--;
                    count++;
                    index++;
                }
            }
            formatString.delete(index, index + 1);
            return count;
        }
        formatString.delete(index, index + 1);
        return 1;
    }

    private static String zeroPad(int inValue, int inMinDigits) {
        Locale locale = Locale.getDefault();
        return String.format(locale, "%0" + inMinDigits + "d", Integer.valueOf(inValue));
    }
}
