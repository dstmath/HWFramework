package com.huawei.android.icu.libcore;

import java.util.Locale;
import libcore.icu.ICU;

public class ICUEx {
    public static String getBestDateTimePattern(String pattern, Locale locale) {
        return ICU.getBestDateTimePattern(pattern, locale);
    }

    public static char[] getDateFormatOrder(String pattern) {
        return ICU.getDateFormatOrder(pattern);
    }
}
