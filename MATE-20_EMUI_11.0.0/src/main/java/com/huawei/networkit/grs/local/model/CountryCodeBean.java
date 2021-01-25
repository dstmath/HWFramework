package com.huawei.networkit.grs.local.model;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.huawei.internal.telephony.PhoneConstantsEx;
import com.huawei.networkit.grs.GrsBaseInfo;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.common.SystemPropUtils;
import java.util.Locale;

public class CountryCodeBean {
    private static final String ANDRIOD_SYSTEMPROP = "android.os.SystemProperties";
    private static final int COUNTRYCODE_SIZE = 2;
    private static final String LOCALE_COUNTRYSYSTEMPROP = "ro.product.locale.region";
    private static final String SPECIAL_COUNTRYCODE_CN = "cn";
    private static final String SPECIAL_COUNTRYCODE_EU = "eu";
    private static final String SPECIAL_COUNTRYCODE_LA = "la";
    private static final String TAG = CountryCodeBean.class.getSimpleName();
    private static final String VENDORCOUNTRY_SYSTEMPROP = "ro.hw.country";
    private String countryCode = "UNKNOWN";
    private String countrySource = "UNKNOWN";

    public CountryCodeBean(Context context, boolean enableNetwork) {
        init(context, enableNetwork);
        this.countryCode = this.countryCode.toUpperCase(Locale.ENGLISH);
    }

    public String getCountrySource() {
        return this.countrySource;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    private void init(Context context, boolean enableNetwork) {
        if (context != null) {
            try {
                getVendorCountryCode();
                if (isCodeValidate()) {
                    Logger.v(TAG, "getCountryCode get country code from {%s}", GrsBaseInfo.CountryCodeSource.VENDOR_COUNTRY);
                    return;
                }
                getSimCountryCode(context, enableNetwork);
                if (isCodeValidate()) {
                    Logger.v(TAG, "getCountryCode get country code from {%s}", GrsBaseInfo.CountryCodeSource.SIM_COUNTRY);
                    return;
                }
                getLocaleCountryCode();
                if (isCodeValidate()) {
                    Logger.v(TAG, "getCountryCode get country code from {%s}", GrsBaseInfo.CountryCodeSource.LOCALE_INFO);
                }
            } catch (Exception e) {
                Logger.w(TAG, "get CountryCode error");
            }
        } else {
            throw new NullPointerException("context must be not null.Please provide app's Context");
        }
    }

    private boolean isCodeValidate() {
        return !"UNKNOWN".equals(this.countryCode);
    }

    private void checkCodeLenth() {
        String str = this.countryCode;
        if (str == null || str.length() != 2) {
            this.countryCode = "UNKNOWN";
            this.countrySource = "UNKNOWN";
        }
    }

    private void getVendorCountryCode() {
        this.countrySource = GrsBaseInfo.CountryCodeSource.VENDOR_COUNTRY;
        this.countryCode = SystemPropUtils.getProperty("get", VENDORCOUNTRY_SYSTEMPROP, ANDRIOD_SYSTEMPROP, "UNKNOWN");
        if (SPECIAL_COUNTRYCODE_EU.equalsIgnoreCase(this.countryCode) || SPECIAL_COUNTRYCODE_LA.equalsIgnoreCase(this.countryCode)) {
            this.countryCode = "UNKNOWN";
            this.countrySource = "UNKNOWN";
            return;
        }
        checkCodeLenth();
    }

    private void getSimCountryCode(Context context, boolean enableNetwork) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(PhoneConstantsEx.PHONE_KEY);
        if (telephonyManager != null) {
            if (!enableNetwork || telephonyManager.getPhoneType() == 2) {
                Logger.v(TAG, "getCountryCode get country code from {%s}", GrsBaseInfo.CountryCodeSource.SIM_COUNTRY);
                this.countryCode = telephonyManager.getSimCountryIso();
                this.countrySource = GrsBaseInfo.CountryCodeSource.SIM_COUNTRY;
            } else {
                Logger.v(TAG, "getCountryCode get country code from {%s}", GrsBaseInfo.CountryCodeSource.NETWORK_COUNTRY);
                this.countryCode = telephonyManager.getNetworkCountryIso();
                this.countrySource = GrsBaseInfo.CountryCodeSource.NETWORK_COUNTRY;
            }
        }
        checkCodeLenth();
    }

    private void getLocaleCountryCode() {
        this.countryCode = SystemPropUtils.getProperty("get", LOCALE_COUNTRYSYSTEMPROP, ANDRIOD_SYSTEMPROP, "UNKNOWN");
        this.countrySource = GrsBaseInfo.CountryCodeSource.LOCALE_INFO;
        if (!SPECIAL_COUNTRYCODE_CN.equalsIgnoreCase(this.countryCode)) {
            Logger.w(TAG, "countryCode from system language is not reliable.");
            this.countryCode = "UNKNOWN";
            this.countrySource = "UNKNOWN";
        }
    }
}
