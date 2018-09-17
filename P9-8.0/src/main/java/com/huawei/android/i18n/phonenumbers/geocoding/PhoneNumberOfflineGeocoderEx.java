package com.huawei.android.i18n.phonenumbers.geocoding;

import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.huawei.android.i18n.phonenumbers.PhonenumberEx.PhoneNumberEx;
import java.util.Locale;

public class PhoneNumberOfflineGeocoderEx {
    private static final Object sLock = new Object();
    private PhoneNumberOfflineGeocoder pnoGeocoder;

    private PhoneNumberOfflineGeocoderEx(PhoneNumberOfflineGeocoder geocoder) {
        this.pnoGeocoder = geocoder;
    }

    public static PhoneNumberOfflineGeocoderEx getInstance() {
        PhoneNumberOfflineGeocoderEx phoneNumberOfflineGeocoderEx;
        synchronized (sLock) {
            phoneNumberOfflineGeocoderEx = new PhoneNumberOfflineGeocoderEx(PhoneNumberOfflineGeocoder.getInstance());
        }
        return phoneNumberOfflineGeocoderEx;
    }

    public String getDescriptionForNumber(PhoneNumberEx number, Locale languageCode) {
        return this.pnoGeocoder.getDescriptionForNumber(number.getPhoneNumber(), languageCode);
    }
}
