package ohos.global.i18n.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class TextRecognitionUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "TextRecognitionUtils");

    public static int[] getAddress(String str) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.utils.TextRecognitionUtilsImpl");
            if (cls == null || (method = cls.getMethod("getAddress", String.class)) == null || (invoke = method.invoke(null, str)) == null) {
                return null;
            }
            return (int[]) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "getAddress failed.", new Object[0]);
            return null;
        }
    }

    public static Date[] convertDate(String str, long j) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.utils.TextRecognitionUtilsImpl");
            if (cls == null || (method = cls.getMethod("convertDate", String.class, Long.TYPE)) == null || (invoke = method.invoke(null, str, Long.valueOf(j))) == null) {
                return null;
            }
            return (Date[]) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "getAddress failed.", new Object[0]);
            return null;
        }
    }

    public static int[] getTime(String str) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.utils.TextRecognitionUtilsImpl");
            if (cls == null || (method = cls.getMethod("getTime", String.class)) == null || (invoke = method.invoke(null, str)) == null) {
                return null;
            }
            return (int[]) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "getTime failed.", new Object[0]);
            return null;
        }
    }

    public static int[] getPhoneNumbers(String str, String str2) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.utils.TextRecognitionUtilsImpl");
            if (cls == null || (method = cls.getMethod("getMatchedPhoneNumber", String.class, String.class)) == null || (invoke = method.invoke(null, str, str2)) == null || !(invoke instanceof int[])) {
                return null;
            }
            return (int[]) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "getMatchedPhoneNumber failed.", new Object[0]);
            return null;
        }
    }
}
