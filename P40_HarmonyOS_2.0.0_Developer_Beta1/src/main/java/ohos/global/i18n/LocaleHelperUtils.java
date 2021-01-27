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

    private static String setCase(Locale locale, String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        int offsetByCodePoints = str.offsetByCodePoints(0, 1);
        return str.substring(0, offsetByCodePoints).toUpperCase(locale) + str.substring(offsetByCodePoints);
    }

    public static ArrayList<Locale> getBlockedLangs(Context context) {
        Method method;
        Object invoke;
        ArrayList<Locale> arrayList = new ArrayList<>();
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlockLangs", Context.class)) == null || (invoke = method.invoke(null, context)) == null || !(invoke instanceof ArrayList)) {
                return arrayList;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlockLangs failed.", new Object[0]);
            return arrayList;
        }
    }

    public static ArrayList<String> getBlockedLangs2String(Context context) {
        Method method;
        Object invoke;
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlockLangs2String", Context.class)) == null || (invoke = method.invoke(null, context)) == null || !(invoke instanceof ArrayList)) {
                return arrayList;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlockLangs2String failed.", new Object[0]);
            return arrayList;
        }
    }

    public static ArrayList<String> getBlockedRegions(Context context, Locale locale) {
        Method method;
        Object invoke;
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlockRegions", Context.class, Locale.class)) == null || (invoke = method.invoke(null, context, locale)) == null || !(invoke instanceof ArrayList)) {
                return arrayList;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlockRegions failed.", new Object[0]);
            return arrayList;
        }
    }

    public static ArrayList<String> getBlockedCities(Context context) {
        Method method;
        Object invoke;
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getBlockCities", Context.class)) == null || (invoke = method.invoke(null, context)) == null || !(invoke instanceof ArrayList)) {
                return arrayList;
            }
            return (ArrayList) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getBlockCities failed.", new Object[0]);
            return arrayList;
        }
    }

    public static String getCityName(String str, Locale locale) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getCityName", String.class, Locale.class)) == null || (invoke = method.invoke(null, str, locale)) == null) {
                return "";
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getCityName failed.", new Object[0]);
            return "";
        }
    }

    public static String replaceCountryName(Locale locale, Locale locale2, String str) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("replaceCountryName", Locale.class, Locale.class, String.class)) == null || (invoke = method.invoke(null, locale, locale2, str)) == null) {
                return "";
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils replaceCountryName failed.", new Object[0]);
            return "";
        }
    }

    public static String getCountryName(Locale locale, Locale locale2, boolean z) {
        String countryName = getCountryName(locale, locale2);
        return z ? setCase(locale2, countryName) : countryName;
    }

    public static String getCountryName(Locale locale, Locale locale2) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getDisplayCountry", Locale.class, Locale.class)) == null || (invoke = method.invoke(null, locale, locale2)) == null) {
                return "";
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getCountryName failed.", new Object[0]);
            return "";
        }
    }

    public static String getLanguageName(Locale locale, Locale locale2, boolean z) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getDisplayName", Locale.class, Locale.class, Boolean.TYPE)) == null || (invoke = method.invoke(null, locale, locale2, Boolean.valueOf(z))) == null) {
                return "";
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getLanguageName failed.", new Object[0]);
            return "";
        }
    }

    public static String getLanguageName(Locale locale, Locale locale2) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.LocaleHelperUtilsImpl");
            if (cls == null || (method = cls.getMethod("getDisplayName", Locale.class, Locale.class, Boolean.TYPE)) == null || (invoke = method.invoke(null, locale, locale2, false)) == null) {
                return "";
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "LocaleHelperUtils getLanguageName failed.", new Object[0]);
            return "";
        }
    }
}
