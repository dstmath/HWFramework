package com.huawei.i18n.tmr.datetime;

import android.util.Log;
import com.huawei.i18n.tmr.datetime.data.LocaleParam;
import java.util.Date;
import java.util.Locale;

public class DateTmr {
    private static final String TAG = "DateTmr";
    private static AbstractDateTmrHandle instance = null;

    private static synchronized AbstractDateTmrHandle getInstance() {
        AbstractDateTmrHandle abstractDateTmrHandle;
        synchronized (DateTmr.class) {
            String locale = calLocale();
            String localeBk = calBkLocale(locale);
            if (instance == null) {
                instance = new DateTmrHandle(locale, localeBk);
            } else if (!instance.getLocale().equals(locale)) {
                instance = new DateTmrHandle(locale, localeBk);
            }
            abstractDateTmrHandle = instance;
        }
        return abstractDateTmrHandle;
    }

    private static String calBkLocale(String locale) {
        if ("en".equals(locale)) {
            return "zh_hans";
        }
        return "en";
    }

    private static String calLocale() {
        Locale defaultLocale = Locale.getDefault();
        String lang = defaultLocale.getLanguage();
        if ("in".equals(lang)) {
            lang = "id";
        }
        if ("iw".equals(lang)) {
            lang = "he";
        }
        String region = defaultLocale.getCountry();
        String langCountry = lang.toLowerCase(Locale.ENGLISH) + "_" + region.toUpperCase(Locale.ENGLISH);
        if ("zh".equalsIgnoreCase(lang)) {
            return "zh_hans";
        }
        if (!LocaleParam.isSupport(langCountry)) {
            langCountry = lang;
        }
        if (!LocaleParam.isSupport(langCountry)) {
            return "en";
        }
        return langCountry;
    }

    public static int[] getTime(String msg) {
        int[] result = {0};
        if (msg == null) {
            return result;
        }
        try {
            return getInstance().getTime(msg);
        } catch (Exception e) {
            Log.e(TAG, "getTime Exception");
            return result;
        }
    }

    public static Date[] convertDate(String msg, long defaultDate) {
        Date[] result = {new Date(defaultDate)};
        if (msg == null) {
            return result;
        }
        try {
            return getInstance().convertDate(msg, defaultDate);
        } catch (Exception e) {
            Log.e(TAG, "convertDate Exception");
            return result;
        }
    }
}
