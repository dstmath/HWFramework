package ohos.global.i18n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class LocaleHelperUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "LocaleHelperUtils");

    public static ArrayList<Locale> getBlackLangs(Context context) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlackLangs", Context.class)) == null || (invoke = method.invoke(null, context)) == null || !(invoke instanceof ArrayList)) {
                return null;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlackLangs failed.", new Object[0]);
            return null;
        }
    }

    public static ArrayList<String> getBlackLangs2String(Context context) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlackLangs2String", Context.class)) == null || (invoke = method.invoke(null, context)) == null || !(invoke instanceof ArrayList)) {
                return null;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlackLangs2String failed.", new Object[0]);
            return null;
        }
    }

    public static ArrayList<String> getBlackRegions(Context context, Locale locale) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlackRegions", Context.class, Locale.class)) == null || (invoke = method.invoke(null, context, locale)) == null || !(invoke instanceof ArrayList)) {
                return null;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlackRegions failed.", new Object[0]);
            return null;
        }
    }

    public static ArrayList<String> getBlackCities(Context context) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlackCities", Context.class)) == null || (invoke = method.invoke(null, context)) == null || !(invoke instanceof ArrayList)) {
                return null;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlackCities failed.", new Object[0]);
            return null;
        }
    }

    public static String getCityName(String str, Locale locale) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getCityName", String.class, Locale.class)) == null || (invoke = method.invoke(null, str, locale)) == null) {
                return null;
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getCityName failed.", new Object[0]);
            return null;
        }
    }

    public static String replaceCountryName(Locale locale, Locale locale2, String str) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("replaceCountryName", Locale.class, Locale.class, String.class)) == null || (invoke = method.invoke(null, locale, locale2, str)) == null) {
                return null;
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils replaceCountryName failed.", new Object[0]);
            return null;
        }
    }

    public static String getDisplayCountry(Locale locale, Locale locale2) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getDisplayCountry", Locale.class, Locale.class)) == null || (invoke = method.invoke(null, locale, locale2)) == null) {
                return null;
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getDisplayCountry failed.", new Object[0]);
            return null;
        }
    }

    public static String getDisplayName(Locale locale, Locale locale2, boolean z) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getDisplayName", Locale.class, Locale.class, Boolean.TYPE)) == null || (invoke = method.invoke(null, locale, locale2, Boolean.valueOf(z))) == null) {
                return null;
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getDisplayName failed.", new Object[0]);
            return null;
        }
    }
}
