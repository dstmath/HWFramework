package ohos.global.i18n;

import java.util.HashMap;
import java.util.Locale;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.text.DateFormatSymbols;
import ohos.global.icu.util.ULocale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LocaleDataImpl extends LocaleData {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "LocaleDataExImpl");
    private static final HashMap<String, LocaleDataImpl> LOCALE_DATA_CACHE = new HashMap<>();
    private String[] longStandAloneWeekdayNames;
    private String[] shortMonthNames;
    private String[] shortStandAloneMonthNames;
    private String[] shortStandAloneWeekdayNames;
    private String timeFormat_Hm;
    private String timeFormat_hm;

    public static LocaleDataImpl getInstance(Locale locale) {
        HiLog.debug(LABEL, "getInstance", new Object[0]);
        if (locale != null) {
            String languageTag = locale.toLanguageTag();
            synchronized (LOCALE_DATA_CACHE) {
                LocaleDataImpl localeDataImpl = LOCALE_DATA_CACHE.get(languageTag);
                if (localeDataImpl != null) {
                    return localeDataImpl;
                }
            }
            HiLog.debug(LABEL, "newLocaleData", new Object[0]);
            LocaleDataImpl initLocaleData = initLocaleData(locale);
            synchronized (LOCALE_DATA_CACHE) {
                LocaleDataImpl localeDataImpl2 = LOCALE_DATA_CACHE.get(languageTag);
                if (localeDataImpl2 != null) {
                    return localeDataImpl2;
                }
                LOCALE_DATA_CACHE.put(languageTag, initLocaleData);
                return initLocaleData;
            }
        }
        throw new NullPointerException("locale == null");
    }

    private static LocaleDataImpl initLocaleData(Locale locale) {
        LocaleDataImpl localeDataImpl = new LocaleDataImpl();
        HiLog.debug(LABEL, "InnewLocaleData", new Object[0]);
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(new ULocale(locale.toLanguageTag()));
        localeDataImpl.shortStandAloneMonthNames = dateFormatSymbols.getMonths(1, 0);
        localeDataImpl.shortMonthNames = dateFormatSymbols.getMonths(0, 0);
        localeDataImpl.shortStandAloneWeekdayNames = dateFormatSymbols.getWeekdays(1, 0);
        localeDataImpl.longStandAloneWeekdayNames = dateFormatSymbols.getWeekdays(1, 1);
        localeDataImpl.timeFormat_hm = ICUImpl.getBestDateTimePattern("hm", locale);
        localeDataImpl.timeFormat_Hm = ICUImpl.getBestDateTimePattern(DateFormat.HOUR24_MINUTE, locale);
        return localeDataImpl;
    }

    @Override // ohos.global.i18n.LocaleData
    public String[] getLongStandAloneWeekdayNames() {
        return this.longStandAloneWeekdayNames;
    }

    @Override // ohos.global.i18n.LocaleData
    public String[] getShortStandAloneMonthNames() {
        return this.shortStandAloneMonthNames;
    }

    @Override // ohos.global.i18n.LocaleData
    public String[] getShortStandAloneWeekdayNames() {
        return this.shortStandAloneWeekdayNames;
    }

    @Override // ohos.global.i18n.LocaleData
    public String getTimeFormat_hm() {
        return this.timeFormat_hm;
    }

    @Override // ohos.global.i18n.LocaleData
    public String getTimeFormat_Hm() {
        return this.timeFormat_Hm;
    }

    @Override // ohos.global.i18n.LocaleData
    public String[] getShortMonthNames() {
        return this.shortMonthNames;
    }
}
