package ohos.global.i18n.phonenumbers;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class PhoneNumberUtil {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "PhoneNumberUtil");

    public abstract AsYouTypeFormatter getAsYouTypeFormatter(String str);

    public abstract PhoneNumber parse(String str, String str2) throws PhoneNumberParseException;

    public static synchronized PhoneNumberUtil getInstance() {
        PhoneNumberUtil phoneNumberUtil;
        synchronized (PhoneNumberUtil.class) {
            phoneNumberUtil = null;
            try {
                Object newInstance = Class.forName("ohos.global.i18n.phonenumbers.PhoneNumberUtilImpl").newInstance();
                if (newInstance != null && (newInstance instanceof PhoneNumberUtil)) {
                    phoneNumberUtil = (PhoneNumberUtil) newInstance;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException unused) {
                HiLog.debug(LABEL, "PhoneNumebrUtil getInstance failed.", new Object[0]);
            }
        }
        return phoneNumberUtil;
    }
}
