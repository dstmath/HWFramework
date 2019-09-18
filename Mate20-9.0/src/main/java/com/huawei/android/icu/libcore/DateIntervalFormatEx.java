package com.huawei.android.icu.libcore;

import libcore.icu.DateIntervalFormat;

public class DateIntervalFormatEx {
    public static String formatDateRange(long startMs, long endMs, int flags, String olsonId) {
        return DateIntervalFormat.formatDateRange(startMs, endMs, flags, olsonId);
    }
}
