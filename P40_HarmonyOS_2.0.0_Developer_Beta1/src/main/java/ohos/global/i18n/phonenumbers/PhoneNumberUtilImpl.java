package ohos.global.i18n.phonenumbers;

import com.huawei.android.i18n.phonenumbers.NumberParseExceptionEx;
import com.huawei.android.i18n.phonenumbers.PhoneNumberUtilEx;
import com.huawei.android.i18n.phonenumbers.PhonenumberEx;
import com.huawei.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoderEx;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PhoneNumberUtilImpl {
    private static final int CACHE_SIZE = 8;
    private static final PhoneNumberOfflineGeocoderEx GEOCODER = PhoneNumberOfflineGeocoderEx.getInstance();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "PhonenumberUtilImpl");
    private static Map<String, PhonenumberEx.PhoneNumberEx> NUMBER_CACHE = Collections.synchronizedMap(new LinkedHashMap<String, PhonenumberEx.PhoneNumberEx>(8, 0.75f, true) {
        /* class ohos.global.i18n.phonenumbers.PhoneNumberUtilImpl.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.util.LinkedHashMap
        public boolean removeEldestEntry(Map.Entry<String, PhonenumberEx.PhoneNumberEx> entry) {
            return size() > 8;
        }
    });
    private static final PhoneNumberUtilEx NUMBER_UTIL = PhoneNumberUtilEx.getInstance();

    public static int size() {
        return NUMBER_CACHE.size();
    }

    public static void clear() {
        NUMBER_CACHE.clear();
    }

    public static InputFormatter getInputFormatter(String str) {
        return new InputFormatterImpl(NUMBER_UTIL.getAsYouTypeFormatter(str));
    }

    public static String getDescriptionForNumber(String str, String str2, Locale locale) {
        PhonenumberEx.PhoneNumberEx phoneNumberEx = NUMBER_CACHE.get(str);
        if (phoneNumberEx != null) {
            return GEOCODER.getDescriptionForNumber(phoneNumberEx, locale);
        }
        try {
            PhonenumberEx.PhoneNumberEx parse = NUMBER_UTIL.parse(str, str2);
            if (parse == null) {
                return "";
            }
            NUMBER_CACHE.put(str, parse);
            return GEOCODER.getDescriptionForNumber(parse, locale);
        } catch (NumberParseExceptionEx unused) {
            HiLog.error(LABEL, "wrong phone numbers ", new Object[0]);
            return "";
        }
    }
}
