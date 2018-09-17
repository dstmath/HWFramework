package com.huawei.android.text.format;

import android.content.Context;
import android.text.format.DateFormat;

public class DateFormatEx {
    public static boolean is24HourFormat(Context context, int userHandle) {
        return DateFormat.is24HourFormat(context, userHandle);
    }

    public static String getTimeFormatString(Context context, int userHandle) {
        return DateFormat.getTimeFormatString(context, userHandle);
    }
}
