package com.huawei.android.icu.util;

import android.icu.util.Calendar;
import android.icu.util.ULocale;

public class CalendarEx {
    public static ULocale getLocale(Calendar cal, ULocale.Type type) {
        if (cal == null) {
            return null;
        }
        return cal.getLocale(type);
    }
}
