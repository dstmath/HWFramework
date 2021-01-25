package ohos.global.i18n.phonenumbers.geocoding;

import com.huawei.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoderEx;
import java.util.Locale;
import ohos.global.i18n.phonenumbers.PhoneNumber;
import ohos.global.i18n.phonenumbers.PhoneNumberImpl;
import ohos.hiviewdfx.HiLogLabel;

public class PhoneNumberOfflineGeocoderImpl extends PhoneNumberOfflineGeocoder {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "geocoder");
    private PhoneNumberOfflineGeocoderEx pnoGeocoder = PhoneNumberOfflineGeocoderEx.getInstance();

    @Override // ohos.global.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
    public String getDescriptionForNumber(PhoneNumber phoneNumber, Locale locale) {
        if (!(phoneNumber instanceof PhoneNumberImpl)) {
            return "";
        }
        return this.pnoGeocoder.getDescriptionForNumber(((PhoneNumberImpl) phoneNumber).getPhoneNumber(), locale);
    }
}
