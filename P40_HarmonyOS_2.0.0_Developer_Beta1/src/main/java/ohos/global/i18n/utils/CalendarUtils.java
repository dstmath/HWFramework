package ohos.global.i18n.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.PersianCalendar;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.UResourceBundleIterator;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class CalendarUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "CalendarUtils");
    private static ICUResourceBundle fBundle = null;

    @Deprecated
    public abstract String getCalendarName(Calendar calendar, ULocale uLocale);

    @Deprecated
    public static CalendarUtils getInstance() {
        try {
            Object newInstance = Class.forName("ohos.global.i18n.utils.CalendarUtilsImpl").newInstance();
            if (newInstance == null || !(newInstance instanceof CalendarUtils)) {
                return null;
            }
            return (CalendarUtils) newInstance;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException unused) {
            HiLog.debug(LABEL, "get CalendarUtils instance failed.", new Object[0]);
            return null;
        }
    }

    public static String getCalendarName(Calendar calendar, Locale locale) {
        if (locale == null) {
            return getCalendarName(calendar, Locale.US);
        }
        ULocale forLocale = ULocale.forLocale(locale);
        ICUResourceBundle iCUResourceBundle = fBundle;
        if ((iCUResourceBundle == null || iCUResourceBundle.getULocale() == null || !fBundle.getULocale().equals(forLocale)) && (UResourceBundle.getBundleInstance(ICUData.ICU_LANG_BASE_NAME, forLocale) instanceof ICUResourceBundle)) {
            fBundle = UResourceBundle.getBundleInstance(ICUData.ICU_LANG_BASE_NAME, forLocale);
        }
        try {
            UResourceBundleIterator iterator = fBundle.getWithFallback("Types/calendar").getIterator();
            while (iterator.hasNext()) {
                UResourceBundle next = iterator.next();
                if (next.getType() == 0 && next.getKey().equals(calendar.getType())) {
                    return next.getString();
                }
            }
        } catch (MissingResourceException unused) {
            HiLog.debug(LABEL, "get data of ICUResource fail", new Object[0]);
        }
        return getCalendarName(calendar, forLocale.getFallback().toLocale());
    }

    public static Calendar getPersianCalendar(Locale locale) {
        return new PersianCalendar(locale);
    }
}
