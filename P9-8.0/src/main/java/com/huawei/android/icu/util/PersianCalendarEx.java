package com.huawei.android.icu.util;

import android.icu.util.Calendar;
import android.icu.util.PersianCalendar;
import java.util.Locale;

public class PersianCalendarEx {
    public static Calendar getPersianCalendar(Locale locale) {
        return new PersianCalendar(locale);
    }
}
