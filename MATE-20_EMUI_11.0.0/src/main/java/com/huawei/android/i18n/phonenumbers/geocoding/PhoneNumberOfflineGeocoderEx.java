package com.huawei.android.i18n.phonenumbers.geocoding;

import android.app.ActivityThread;
import android.common.HwFrameworkFactory;
import android.content.Context;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.huawei.android.i18n.phonenumbers.PhonenumberEx;
import com.huawei.android.os.SystemPropertiesEx;
import huawei.com.android.internal.app.HwLocaleHelperEx;
import java.util.Locale;

public class PhoneNumberOfflineGeocoderEx {
    private static final String HANS_TW = "台湾";
    private static final String HANT_TW = "台灣";
    private static final String IS_TW_VERSION = "tw";
    private static final String LANGUAGE_ZH = "zh";
    private static final String TAIWAN_VERSION = "hbc.country";
    private static final int TEL_AREA_CODE_TW = 886;
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

    public String getDescriptionForNumber(PhonenumberEx.PhoneNumberEx number, Locale languageCode) {
        PhoneNumberOfflineGeocoder phoneNumberOfflineGeocoder;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (number == null || (phoneNumberOfflineGeocoder = this.pnoGeocoder) == null || phoneUtil == null || languageCode == null) {
            return null;
        }
        String phoneNumberlocation = phoneNumberOfflineGeocoder.getDescriptionForNumber(number.getPhoneNumber(), languageCode);
        String twRegion = SystemPropertiesEx.get(TAIWAN_VERSION, "");
        if (number.getPhoneNumber().getCountryCode() == TEL_AREA_CODE_TW) {
            if (!phoneUtil.isValidNumber(number.getPhoneNumber())) {
                return " ";
            }
            if ((!LANGUAGE_ZH.equals(languageCode.getLanguage()) || HANS_TW.equals(phoneNumberlocation) || HANT_TW.equals(phoneNumberlocation)) && IS_TW_VERSION.equalsIgnoreCase(twRegion)) {
                return " ";
            }
        }
        String countryName = phoneUtil.getRegionCodeForNumber(number.getPhoneNumber());
        if (countryName == null || "".equals(countryName)) {
            return phoneNumberlocation;
        }
        String phoneNumberlocation2 = HwFrameworkFactory.getHwLocaleHelperEx().replaceCountryName(new Locale(Locale.getDefault().getLanguage(), countryName), languageCode, phoneNumberlocation);
        Context context = ActivityThread.currentApplication();
        if (context == null || !HwLocaleHelperEx.getTabooBlackAllRegionsPart(context, languageCode).contains(countryName) || !phoneNumberlocation2.equals(new Locale(Locale.getDefault().getLanguage(), countryName).getDisplayCountry(languageCode))) {
            return phoneNumberlocation2;
        }
        return "";
    }
}
