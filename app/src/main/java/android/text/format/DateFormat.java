package android.text.format;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.RILConstants;
import com.huawei.pgmng.log.LogPower;
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
    private static final Object sLocaleLock = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.format.DateFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.format.DateFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.format.DateFormat.<clinit>():void");
    }

    public static boolean is24HourFormat(Context context) {
        return is24HourFormat(context, UserHandle.myUserId());
    }

    public static boolean is24HourFormat(Context context, int userHandle) {
        String value = System.getStringForUser(context.getContentResolver(), "time_12_24", userHandle);
        if (value != null) {
            return value.equals("24");
        }
        Locale locale = context.getResources().getConfiguration().locale;
        synchronized (sLocaleLock) {
            if (sIs24HourLocale == null || !sIs24HourLocale.equals(locale)) {
                java.text.DateFormat natural = java.text.DateFormat.getTimeInstance(1, locale);
                if (!(natural instanceof SimpleDateFormat)) {
                    value = "12";
                } else if (((SimpleDateFormat) natural).toPattern().indexOf(72) >= 0) {
                    value = "24";
                } else {
                    value = "12";
                }
                synchronized (sLocaleLock) {
                    sIs24HourLocale = locale;
                    sIs24Hour = value.equals("24");
                }
                return sIs24Hour;
            }
            boolean z = sIs24Hour;
            return z;
        }
    }

    public static String getBestDateTimePattern(Locale locale, String skeleton) {
        return ICU.getBestDateTimePattern(skeleton, locale);
    }

    public static java.text.DateFormat getTimeFormat(Context context) {
        return new SimpleDateFormat(getTimeFormatString(context));
    }

    public static String getTimeFormatString(Context context) {
        return getTimeFormatString(context, UserHandle.myUserId());
    }

    public static String getTimeFormatString(Context context, int userHandle) {
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        return is24HourFormat(context, userHandle) ? d.timeFormat_Hm : d.timeFormat_hm;
    }

    public static java.text.DateFormat getDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(3);
    }

    public static java.text.DateFormat getLongDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(1);
    }

    public static java.text.DateFormat getMediumDateFormat(Context context) {
        return java.text.DateFormat.getDateInstance(2);
    }

    public static char[] getDateFormatOrder(Context context) {
        return ICU.getDateFormatOrder(getDateFormatString());
    }

    private static String getDateFormatString() {
        java.text.DateFormat df = java.text.DateFormat.getDateInstance(3);
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

    public static boolean hasSeconds(CharSequence inFormat) {
        return hasDesignator(inFormat, SECONDS);
    }

    public static boolean hasDesignator(CharSequence inFormat, char designator) {
        if (inFormat == null) {
            return false;
        }
        int length = inFormat.length();
        int i = 0;
        while (i < length) {
            int count = 1;
            char c = inFormat.charAt(i);
            if (c == '\'') {
                count = skipQuotedText(inFormat, i, length);
            } else if (c == designator) {
                return true;
            }
            i += count;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int skipQuotedText(CharSequence s, int i, int len) {
        if (i + 1 < len && s.charAt(i + 1) == QUOTE) {
            return 2;
        }
        int count = 1;
        i++;
        while (i < len) {
            if (s.charAt(i) == QUOTE) {
                count++;
                if (i + 1 < len && s.charAt(i + 1) == QUOTE) {
                    i++;
                }
            } else {
                i++;
                count++;
            }
        }
        return count;
    }

    public static CharSequence format(CharSequence inFormat, Calendar inDate) {
        SpannableStringBuilder s = new SpannableStringBuilder(inFormat);
        LocaleData localeData = LocaleData.get(Locale.getDefault());
        int len = inFormat.length();
        int i = 0;
        while (i < len) {
            int count = 1;
            char c = s.charAt(i);
            if (c == '\'') {
                count = appendQuotedText(s, i, len);
                len = s.length();
            } else {
                CharSequence replacement;
                while (i + count < len && s.charAt(i + count) == c) {
                    count++;
                }
                switch (c) {
                    case RILConstants.RIL_REQUEST_SET_BAND_MODE /*65*/:
                    case RILConstants.RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM /*97*/:
                        replacement = localeData.amPm[inDate.get(9) + 0];
                        break;
                    case RILConstants.RIL_REQUEST_STK_SEND_ENVELOPE_COMMAND /*69*/:
                    case RILConstants.RIL_REQUEST_EXIT_EMERGENCY_CALLBACK_MODE /*99*/:
                        replacement = getDayOfWeekString(localeData, inDate.get(7), count, c);
                        break;
                    case RILConstants.RIL_REQUEST_EXPLICIT_CALL_TRANSFER /*72*/:
                    case LogPower.GAMEOF3D_PAUSED /*107*/:
                        replacement = zeroPad(inDate.get(11), count);
                        break;
                    case RILConstants.RIL_REQUEST_GET_NEIGHBORING_CELL_IDS /*75*/:
                    case LogPower.WEBPAGE_STARTED /*104*/:
                        int hour = inDate.get(10);
                        if (c == LogPower.WEBPAGE_STARTED && hour == 0) {
                            hour = 12;
                        }
                        replacement = zeroPad(hour, count);
                        break;
                    case RILConstants.RIL_REQUEST_SET_LOCATION_UPDATES /*76*/:
                    case RILConstants.RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE /*77*/:
                        replacement = getMonthString(localeData, inDate.get(2), count, c);
                        break;
                    case LogPower.ACTIVITY_RESUMED /*100*/:
                        replacement = zeroPad(inDate.get(5), count);
                        break;
                    case LogPower.APP_LAUNCHER /*109*/:
                        replacement = zeroPad(inDate.get(12), count);
                        break;
                    case LogPower.ALARM_BLOCKED /*115*/:
                        replacement = zeroPad(inDate.get(13), count);
                        break;
                    case LogPower.ALARM_START /*121*/:
                        replacement = getYearString(inDate.get(1), count);
                        break;
                    case LogPower.NOTIFICATION_ENQUEUE /*122*/:
                        replacement = getTimeZoneString(inDate, count);
                        break;
                    default:
                        replacement = null;
                        break;
                }
                if (replacement != null) {
                    s.replace(i, i + count, replacement);
                    count = replacement.length();
                    len = s.length();
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
        if (count == 5) {
            return standalone ? ld.tinyStandAloneWeekdayNames[day] : ld.tinyWeekdayNames[day];
        } else if (count == 4) {
            return standalone ? ld.longStandAloneWeekdayNames[day] : ld.longWeekdayNames[day];
        } else {
            return standalone ? ld.shortStandAloneWeekdayNames[day] : ld.shortWeekdayNames[day];
        }
    }

    private static String getMonthString(LocaleData ld, int month, int count, int kind) {
        boolean standalone = kind == 76;
        if (count == 5) {
            return standalone ? ld.tinyStandAloneMonthNames[month] : ld.tinyMonthNames[month];
        } else if (count == 4) {
            return standalone ? ld.longStandAloneMonthNames[month] : ld.longMonthNames[month];
        } else if (count != 3) {
            return zeroPad(month + 1, count);
        } else {
            return standalone ? ld.shortStandAloneMonthNames[month] : ld.shortMonthNames[month];
        }
    }

    private static String getTimeZoneString(Calendar inDate, int count) {
        TimeZone tz = inDate.getTimeZone();
        if (count < 2) {
            return formatZoneOffset(inDate.get(16) + inDate.get(15), count);
        }
        boolean dst;
        if (inDate.get(16) != 0) {
            dst = true;
        } else {
            dst = false;
        }
        return tz.getDisplayName(dst, 0);
    }

    private static String formatZoneOffset(int offset, int count) {
        offset /= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED;
        StringBuilder tb = new StringBuilder();
        if (offset < 0) {
            tb.insert(0, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            offset = -offset;
        } else {
            tb.insert(0, "+");
        }
        int minutes = (offset % 3600) / 60;
        tb.append(zeroPad(offset / 3600, 2));
        tb.append(zeroPad(minutes, 2));
        return tb.toString();
    }

    private static String getYearString(int year, int count) {
        if (count == 2) {
            return zeroPad(year % 100, 2);
        }
        return String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(year)});
    }

    private static int appendQuotedText(SpannableStringBuilder s, int i, int len) {
        if (i + 1 >= len || s.charAt(i + 1) != QUOTE) {
            int count = 0;
            s.delete(i, i + 1);
            len--;
            while (i < len) {
                if (s.charAt(i) != QUOTE) {
                    i++;
                    count++;
                } else if (i + 1 >= len || s.charAt(i + 1) != QUOTE) {
                    s.delete(i, i + 1);
                    break;
                } else {
                    s.delete(i, i + 1);
                    len--;
                    count++;
                    i++;
                }
            }
            return count;
        }
        s.delete(i, i + 1);
        return 1;
    }

    private static String zeroPad(int inValue, int inMinDigits) {
        return String.format(Locale.getDefault(), "%0" + inMinDigits + "d", new Object[]{Integer.valueOf(inValue)});
    }
}
