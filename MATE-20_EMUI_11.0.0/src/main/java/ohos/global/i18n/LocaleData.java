package ohos.global.i18n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class LocaleData {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "LocaleData");

    public abstract String[] getLongStandAloneWeekdayNames();

    public abstract String[] getShortMonthNames();

    public abstract String[] getShortStandAloneMonthNames();

    public abstract String[] getShortStandAloneWeekdayNames();

    public abstract String getTimeFormat_Hm();

    public abstract String getTimeFormat_hm();

    public static LocaleData get(Locale locale) {
        Object invoke;
        try {
            Method method = Class.forName("ohos.global.i18n.LocaleDataImpl").getMethod("getInstance", Locale.class);
            if (method == null || (invoke = method.invoke(null, locale)) == null || !(invoke instanceof LocaleData)) {
                return null;
            }
            return (LocaleData) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleData get failed.", new Object[0]);
            return null;
        }
    }
}
