package com.huawei.wallet.sdk.common.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class PaymentCodePreferences {
    public static final String ALIPAY_USER_INFO = "alipay_user_info";
    public static final String AUTH_LICENSE_AGREE = "license_agree";
    public static final String NATIVE_AD_IMAGECACHE_KEY = "native_ad_imagecache";
    public static final String PAYMENT_EVENT_DATA = "payment_events";
    private static final String PREFERENCES_NAME = "payment_code";
    public static final String PUSH_TOKEN = "push_token";
    private static final byte[] SYNC_LOCK = new byte[0];
    public static final String UUID_NUMBER = "uuid_number";
    private static volatile PaymentCodePreferences instance = null;
    private SharedPreferences sp = null;

    public PaymentCodePreferences(Context mContext) {
        this.sp = mContext.getSharedPreferences(PREFERENCES_NAME, 0);
    }

    public static PaymentCodePreferences getInstance(Context mContext) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new PaymentCodePreferences(mContext.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public boolean isContainsKey(String key) {
        return this.sp.contains(key);
    }

    public int getValue(String key, int defValue) {
        return this.sp.getInt(key, defValue);
    }

    public boolean getValue(String key, boolean defValue) {
        return this.sp.getBoolean(key, defValue);
    }

    public String getValue(String key, String defValue) {
        return this.sp.getString(key, defValue);
    }

    public void setValue(String key, int value) {
        this.sp.edit().putInt(key, value).commit();
    }

    public void setValue(String key, boolean value) {
        this.sp.edit().putBoolean(key, value).commit();
    }

    public void setValue(String key, String value) {
        this.sp.edit().putString(key, value).commit();
    }

    public boolean contains(String key) {
        return this.sp.contains(key);
    }

    public void remove(String key) {
        this.sp.edit().remove(key).commit();
    }
}
