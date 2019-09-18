package com.huawei.android.icu.libcore;

import java.util.Locale;
import libcore.icu.LocaleData;

public class LocaleDataEx {
    private LocaleData mLocaleData;

    public LocaleDataEx(LocaleData localeData) {
        this.mLocaleData = localeData;
    }

    public String[] getLongStandAloneWeekdayNames() {
        return (String[]) this.mLocaleData.longStandAloneWeekdayNames.clone();
    }

    public Integer getFirstDayOfWeek() {
        return this.mLocaleData.firstDayOfWeek;
    }

    public Integer getMinimalDaysInFirstWeek() {
        return this.mLocaleData.minimalDaysInFirstWeek;
    }

    public String[] getShortStandAloneMonthNames() {
        return (String[]) this.mLocaleData.shortStandAloneMonthNames.clone();
    }

    public static LocaleDataEx get(Locale locale) {
        return new LocaleDataEx(LocaleData.get(locale));
    }

    public static Locale mapInvalidAndNullLocales(Locale locale) {
        return LocaleData.mapInvalidAndNullLocales(locale);
    }

    public String[] getShortStandAloneWeekdayNames() {
        return (String[]) this.mLocaleData.shortStandAloneWeekdayNames.clone();
    }

    public String getTimeFormat_hm() {
        return this.mLocaleData.timeFormat_hm;
    }

    public String getTimeFormat_Hm() {
        return this.mLocaleData.timeFormat_Hm;
    }

    public String[] getShortMonthNames() {
        return (String[]) this.mLocaleData.shortMonthNames.clone();
    }

    public char getZeroDigit() {
        return this.mLocaleData.zeroDigit;
    }
}
