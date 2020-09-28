package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.Date;
import java.util.Locale;

public class DateTmr {
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
        if (locale.equals("en")) {
            return "zh_hans";
        }
        return "en";
    }

    private static String calLocale() {
        Locale d = Locale.getDefault();
        String lang = d.getLanguage();
        if (lang.equals("in")) {
            lang = "id";
        }
        if (lang.equals("iw")) {
            lang = "he";
        }
        String region = d.getCountry();
        String l = String.valueOf(lang.toLowerCase(Locale.ENGLISH)) + "_" + region.toUpperCase(Locale.ENGLISH);
        if (lang.equalsIgnoreCase("zh")) {
            return "zh_hans";
        }
        if (!LocaleParam.isSupport(l)) {
            l = lang;
        }
        if (!LocaleParam.isSupport(l)) {
            return "en";
        }
        return l;
    }

    public static int[] getTime(String msg) {
        return getInstance().getTime(msg);
    }

    public static Date[] convertDate(String msg, long defaultDate) {
        return getInstance().convertDate(msg, defaultDate);
    }
}
