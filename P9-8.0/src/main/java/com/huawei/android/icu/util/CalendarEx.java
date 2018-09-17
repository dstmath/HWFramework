package com.huawei.android.icu.util;

import android.icu.util.Calendar;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Type;

public class CalendarEx {
    public static ULocale getLocale(Calendar cal, Type type) {
        if (cal == null) {
            return null;
        }
        return cal.getLocale(type);
    }
}
