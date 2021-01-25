package ohos.global.text.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class DateFormatUtil {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "DateFormatUtil");

    public static boolean is24HourFormat(Context context) {
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
}
