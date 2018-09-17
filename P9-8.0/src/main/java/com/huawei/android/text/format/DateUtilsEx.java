package com.huawei.android.text.format;

import android.content.Context;
import huawei.android.text.format.HwDateUtils;
import java.util.Formatter;

public class DateUtilsEx extends HwDateUtils {
    public static String formatChinaDateTime(Context context, long timeInMills, int flag) {
        return HwDateUtils.formatChinaDateTime(context, timeInMills, flag);
    }

    public static String formatChinaDateRange(Context context, Formatter formatter, long startMills, long endMills, int flags, String timeZone) {
        return HwDateUtils.formatChinaDateRange(context, formatter, startMills, endMills, flags, timeZone);
    }
}
