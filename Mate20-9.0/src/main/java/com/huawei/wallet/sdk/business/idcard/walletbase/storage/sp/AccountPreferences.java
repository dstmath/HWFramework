package com.huawei.wallet.sdk.business.idcard.walletbase.storage.sp;

import android.content.Context;
import android.content.SharedPreferences;

public final class AccountPreferences {
    public static final String ACCOUNT_HEADURL = "account_headurl";
    public static final String ACCOUNT_SERVICE_COUNTRY_CODE = "account_service_country_code";
    public static final String LAST_USER_ID = "last_user_id";
    private static final String PREFERENCES_NAME = "wallet_account";
    private static final byte[] SYNC_LOCK = new byte[0];
    public static final String USER_ID = "user_id";
    private static volatile AccountPreferences instance = null;
    private SharedPreferences sp = null;

    private AccountPreferences(Context context) {
        if (context != null) {
            this.sp = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, 0);
        }
    }

    public static AccountPreferences getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new AccountPreferences(context);
                }
            }
        }
        return instance;
    }

    public boolean putString(String key, String value) {
        if (this.sp == null) {
            return false;
        }
        return this.sp.edit().putString(key, value).commit();
    }

    public String getString(String key, String defaultValue) {
        if (this.sp == null) {
            return defaultValue;
        }
        return this.sp.getString(key, defaultValue);
    }

    public boolean putBoolean(String key, Boolean value) {
        if (this.sp == null) {
            return false;
        }
        return this.sp.edit().putBoolean(key, value.booleanValue()).commit();
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        if (this.sp == null) {
            return defaultValue;
        }
        return Boolean.valueOf(this.sp.getBoolean(key, defaultValue.booleanValue()));
    }
}
