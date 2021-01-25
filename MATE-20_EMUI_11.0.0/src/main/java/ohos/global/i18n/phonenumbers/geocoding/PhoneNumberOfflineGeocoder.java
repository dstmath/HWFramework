package ohos.global.i18n.phonenumbers.geocoding;

import java.util.Locale;
import ohos.global.i18n.phonenumbers.PhoneNumber;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class PhoneNumberOfflineGeocoder {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "PhoneNumberOfflineGeocoder");

    public abstract String getDescriptionForNumber(PhoneNumber phoneNumber, Locale locale);

    public static synchronized PhoneNumberOfflineGeocoder getInstance() {
        PhoneNumberOfflineGeocoder phoneNumberOfflineGeocoder;
        synchronized (PhoneNumberOfflineGeocoder.class) {
            phoneNumberOfflineGeocoder = null;
            try {
                Object newInstance = Class.forName("ohos.global.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoderImpl").newInstance();
                if (newInstance != null && (newInstance instanceof PhoneNumberOfflineGeocoder)) {
                    phoneNumberOfflineGeocoder = (PhoneNumberOfflineGeocoder) newInstance;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException unused) {
                HiLog.debug(LABEL, "PhoneNumebrUtil getInstance failed.", new Object[0]);
            }
        }
        return phoneNumberOfflineGeocoder;
    }
}
