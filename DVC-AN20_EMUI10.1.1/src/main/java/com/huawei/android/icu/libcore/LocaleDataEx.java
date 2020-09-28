package com.huawei.android.icu.libcore;

import java.util.Locale;
import libcore.icu.LocaleData;

public class LocaleDataEx {
    private LocaleData mLocaleData;
    private Integer mMinimalDaysInFirstWeek = 0;

    public LocaleDataEx(LocaleData localeData) {
        this.mLocaleData = localeData;
    }

    public String[] getLongStandAloneWeekdayNames() {
        LocaleData localeData = this.mLocaleData;
        if (localeData != null) {
            return (String[]) localeData.longStandAloneWeekdayNames.clone();
        }
        return null;
    }

    public Integer getFirstDayOfWeek() {
        LocaleData localeData = this.mLocaleData;
        if (localeData != null) {
            return localeData.firstDayOfWeek;
        }
        return null;
    }

    public Integer getMinimalDaysInFirstWeek() {
        this.mMinimalDaysInFirstWeek = 0;
        return 0;
    }

    public String[] getShortStandAloneMonthNames() {
        LocaleData localeData = this.mLocaleData;
        if (localeData != null) {
            return (String[]) localeData.shortStandAloneMonthNames.clone();
        }
        return null;
    }

    public static LocaleDataEx get(Locale locale) {
        return new LocaleDataEx(LocaleData.get(locale));
    }

    public static Locale mapInvalidAndNullLocales(Locale locale) {
        if (locale == null) {
            return Locale.getDefault();
        }
        if ("und".equals(locale.toLanguageTag())) {
            return Locale.ROOT;
        }
        return locale;
    }

    public String[] getShortStandAloneWeekdayNames() {
        LocaleData localeData = this.mLocaleData;
        if (localeData != null) {
            return (String[]) localeData.shortStandAloneWeekdayNames.clone();
        }
        return null;
    }

    public String getTimeFormat_hm() {
        LocaleData localeData = this.mLocaleData;
        if (localeData != null) {
            return localeData.timeFormat_hm;
        }
        return null;
    }

    public String getTimeFormat_Hm() {
        LocaleData localeData = this.mLocaleData;
        if (localeData != null) {
            return localeData.timeFormat_Hm;
        }
        return null;
    }

    public String[] getShortMonthNames() {
        LocaleData localeData = this.mLocaleData;
        if (localeData != null) {
            return (String[]) localeData.shortMonthNames.clone();
        }
        return null;
    }

    public char getZeroDigit() {
        LocaleData localeData = this.mLocaleData;
        if (localeData == null) {
            return 0;
        }
        return localeData.zeroDigit;
    }
}
