package ohos.global.i18n.phonenumbers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class InputFormatter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "InputFormatter");

    public abstract void clean();

    public abstract int getPosition();

    public abstract String inputNumber(char c);

    public abstract String inputNumberAndRememberPosition(char c);

    public static InputFormatter getInstance(String str) {
        Method method;
        Object invoke;
        try {
            Class<?> cls = Class.forName("ohos.global.i18n.phonenumbers.PhoneNumberUtilImpl");
            if (cls == null || (method = cls.getMethod("getInputFormatter", String.class)) == null || (invoke = method.invoke(null, str)) == null || !(invoke instanceof InputFormatter)) {
                return null;
            }
            return (InputFormatter) invoke;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.debug(LABEL, "InputFormatter getInstance failed.", new Object[0]);
            return null;
        }
    }
}
