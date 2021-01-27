package ohos.global.i18n.text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import ohos.app.Context;
import ohos.global.icu.text.DateFormatSymbols;
import ohos.global.icu.util.ULocale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DateFormatUtil {
    private static final int CACHE_SIZE = 8;
    private static Map<Locale, LocaleDateData> DATA_CACHE = Collections.synchronizedMap(new LinkedHashMap<Locale, LocaleDateData>(8, 0.75f, true) {
        /* class ohos.global.i18n.text.DateFormatUtil.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.util.LinkedHashMap
        public boolean removeEldestEntry(Map.Entry<Locale, LocaleDateData> entry) {
            return size() > 8;
        }
    });
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "DateFormatUtil");

    public static String getBestPattern(String str, Locale locale) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.ICUImpl");
            if (cls == null || (method = cls.getMethod("getBestDateTimePattern", String.class, Locale.class)) == null || (invoke = method.invoke(null, str, locale)) == null) {
                return null;
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "ICU getBestPattern failed.", new Object[0]);
            return null;
        }
    }

    public static char[] getOrder(String str) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.ICUImpl");
            if (cls == null || (method = cls.getMethod("getDateFormatOrder", String.class)) == null || (invoke = method.invoke(null, str)) == null || !(invoke instanceof char[])) {
                return null;
            }
            return (char[]) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "ICU getOrder failed.", new Object[0]);
            return null;
        }
    }

    public static boolean is24HourClock(Context context) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.text.format.DateFormatUtilImpl");
            if (cls == null || (method = cls.getMethod("is24HourFormat", Context.class)) == null || (invoke = method.invoke(null, context)) == null || !(invoke instanceof Boolean)) {
                return false;
            }
            return ((Boolean) invoke).booleanValue();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "DateFormatUtil is24HourFormat failed.", new Object[0]);
            return false;
        }
    }

    public static String[] getLongStandAloneWeekdayNames(Locale locale) {
        LocaleDateData localeDateData = DATA_CACHE.get(locale);
        if (localeDateData == null) {
            localeDateData = new LocaleDateData(locale);
            DATA_CACHE.put(locale, localeDateData);
        }
        return localeDateData.getLongStandAloneWeekdayNames();
    }

    public static String[] getShortStandAloneWeekdayNames(Locale locale) {
        LocaleDateData localeDateData = DATA_CACHE.get(locale);
        if (localeDateData == null) {
            localeDateData = new LocaleDateData(locale);
            DATA_CACHE.put(locale, localeDateData);
        }
        return localeDateData.getShortStandAloneWeekdayNames();
    }

    public static String[] getShortStandAloneMonthNames(Locale locale) {
        LocaleDateData localeDateData = DATA_CACHE.get(locale);
        if (localeDateData == null) {
            localeDateData = new LocaleDateData(locale);
            DATA_CACHE.put(locale, localeDateData);
        }
        return localeDateData.getShortStandAloneMonthNames();
    }

    public static String[] getShortMonthNames(Locale locale) {
        LocaleDateData localeDateData = DATA_CACHE.get(locale);
        if (localeDateData == null) {
            localeDateData = new LocaleDateData(locale);
            DATA_CACHE.put(locale, localeDateData);
        }
        return localeDateData.getShortMonthNames();
    }

    public static String getTimeFormat_hm(Locale locale) {
        LocaleDateData localeDateData = DATA_CACHE.get(locale);
        if (localeDateData == null) {
            localeDateData = new LocaleDateData(locale);
            DATA_CACHE.put(locale, localeDateData);
        }
        return localeDateData.getTimeFormat_hm();
    }

    public static String getTimeFormat_Hm(Locale locale) {
        LocaleDateData localeDateData = DATA_CACHE.get(locale);
        if (localeDateData == null) {
            localeDateData = new LocaleDateData(locale);
            DATA_CACHE.put(locale, localeDateData);
        }
        return localeDateData.getTimeFormat_Hm();
    }

    public static String format(String str, String str2, String str3, long j, long j2) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.ICUImpl");
            if (cls == null || (method = cls.getMethod("formatRange", String.class, String.class, String.class, Long.TYPE, Long.TYPE)) == null || (invoke = method.invoke(null, str, str2, str3, Long.valueOf(j), Long.valueOf(j2))) == null) {
                return null;
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "ICU format Range failed.", new Object[0]);
            return null;
        }
    }

    private static class LocaleDateData {
        private String[] longStandAloneWeekdayNames;
        private String[] shortMonthNames;
        private String[] shortStandAloneMonthNames;
        private String[] shortStandAloneWeekdayNames;
        private String timeFormat_Hm;
        private String timeFormat_hm;

        public LocaleDateData(Locale locale) {
            initLocaleData(locale);
        }

        private void initLocaleData(Locale locale) {
            DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(ULocale.forLocale(locale));
            this.shortStandAloneMonthNames = dateFormatSymbols.getMonths(1, 0);
            this.shortMonthNames = dateFormatSymbols.getMonths(0, 0);
            this.shortStandAloneWeekdayNames = dateFormatSymbols.getWeekdays(1, 0);
            this.longStandAloneWeekdayNames = dateFormatSymbols.getWeekdays(1, 1);
            this.timeFormat_hm = DateFormatUtil.getBestPattern("hm", locale);
            this.timeFormat_Hm = DateFormatUtil.getBestPattern("Hm", locale);
        }

        public String[] getLongStandAloneWeekdayNames() {
            return this.longStandAloneWeekdayNames;
        }

        public String[] getShortStandAloneMonthNames() {
            return this.shortStandAloneMonthNames;
        }

        public String[] getShortStandAloneWeekdayNames() {
            return this.shortStandAloneWeekdayNames;
        }

        public String getTimeFormat_hm() {
            return this.timeFormat_hm;
        }

        public String getTimeFormat_Hm() {
            return this.timeFormat_Hm;
        }

        public String[] getShortMonthNames() {
            return this.shortMonthNames;
        }
    }
}
