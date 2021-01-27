package com.huawei.android.icu.util;

import android.icu.util.Calendar;
import android.icu.util.ULocale;

public class CalendarEx {
    private ULocale mULocale;

    public static final class Type {
        private Type() {
        }
    }

    public static ULocale getLocale(Calendar cal, Type type) {
        if (cal != null) {
            return ULocale.getDefault();
        }
        return null;
    }
}
