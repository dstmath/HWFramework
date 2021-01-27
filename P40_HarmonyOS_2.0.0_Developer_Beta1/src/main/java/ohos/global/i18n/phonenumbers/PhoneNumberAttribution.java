package ohos.global.i18n.phonenumbers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PhoneNumberAttribution {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "PhoneNumberAttribution");

    public static String getAttribute(String str, String str2, Locale locale) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.phonenumbers.PhoneNumberUtilImpl");
            if (cls == null || (method = cls.getMethod("getDescriptionForNumber", String.class, String.class, Locale.class)) == null || (invoke = method.invoke(null, str, str2, locale)) == null || !(invoke instanceof String)) {
                return "";
            }
            return (String) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "get Attribution Desfailed.", new Object[0]);
            return "";
        }
    }
}
