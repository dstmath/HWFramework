package ohos.global.i18n.utils;

import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.ULocale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class CalendarUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "CalendarUtils");

    public abstract String getCalendarName(Calendar calendar, ULocale uLocale);

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
}
