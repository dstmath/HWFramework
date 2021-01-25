package ohos.global.i18n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class ICU {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ICU");

    public static String getBestDateTimePattern(String str, Locale locale) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.ICUImpl");
            if (cls == null || (method = cls.getMethod("getBestDateTimePattern", String.class, Locale.class)) == null || (invoke = method.invoke(null, str, locale)) == null) {
                return null;
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "ICU getBestDateTimePattern failed.", new Object[0]);
            return null;
        }
    }

    public static char[] getDateFormatOrder(String str) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.ICUImpl");
            if (cls == null || (method = cls.getMethod("getDateFormatOrder", String.class)) == null || (invoke = method.invoke(null, str)) == null || !(invoke instanceof char[])) {
                return null;
            }
            return (char[]) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "ICU getDateFormatOrder failed.", new Object[0]);
            return null;
        }
    }
}
