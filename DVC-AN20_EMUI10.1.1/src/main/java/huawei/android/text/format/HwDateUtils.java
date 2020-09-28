package huawei.android.text.format;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import java.util.Formatter;
import java.util.Locale;

public class HwDateUtils extends DateUtils {
    private static final int DEFAULT_CHINA_DATE_SIZE = 50;

    public static String formatChinaDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags, String timeZone) {
        String date = formatDateRange(context, formatter, startMillis, endMillis, flags, timeZone).toString();
        if (DateFormat.is24HourFormat(context)) {
            return date;
        }
        return formatChinaDateTime(context, date);
    }

    public static String formatChinaDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags) {
        return formatChinaDateRange(context, formatter, startMillis, endMillis, flags, null);
    }

    public static String formatChinaDateRange(Context context, long startMillis, long endMillis, int flags) {
        return formatChinaDateRange(context, new Formatter(new StringBuilder(50), Locale.getDefault()), startMillis, endMillis, flags);
    }

    public static String formatChinaDateTime(Context context, long millis, int flags) {
        return formatChinaDateRange(context, millis, millis, flags);
    }

    public static String formatChinaDateTime(Context context, String normalTime) {
        Locale defaultLocale = Locale.getDefault();
        Resources resources = context.getResources();
        String[] normal12Times = resources.getStringArray(33816581);
        String[] chinaTimes = resources.getStringArray(33816582);
        if (!(Locale.SIMPLIFIED_CHINESE.equals(defaultLocale) || Locale.forLanguageTag("zh-Hans-CN").equals(defaultLocale))) {
            return normalTime;
        }
        int size = normal12Times.length;
        for (int i = 0; i < size; i++) {
            if (normalTime.contains(normal12Times[i])) {
                return normalTime.replace(normal12Times[i], chinaTimes[i]);
            }
        }
        return normalTime;
    }
}
