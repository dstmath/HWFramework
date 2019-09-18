package com.huawei.wallet.sdk.common.storage;

import android.content.Context;
import android.content.SharedPreferences;

public final class NFCPreferences {
    private static final String HOTALK_PROPERTIES_NAME = "NFC_Properties";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile NFCPreferences instance = null;
    private SharedPreferences sp = null;

    private NFCPreferences(Context context) {
        this.sp = context.getApplicationContext().getSharedPreferences(HOTALK_PROPERTIES_NAME, 0);
    }

    public static NFCPreferences getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new NFCPreferences(context);
                }
            }
        }
        return instance;
    }

    public boolean remove(String key) {
        return this.sp.edit().remove(key).commit();
    }

    public boolean putLong(String key, Long value) {
        return this.sp.edit().putLong(key, value.longValue()).commit();
    }

    public Long getLong(String key, Long defaultValue) {
        return Long.valueOf(this.sp.getLong(key, defaultValue.longValue()));
    }

    public boolean putString(String key, String value) {
        return this.sp.edit().putString(key, value).commit();
    }

    public String getString(String key, String defaultValue) {
        return this.sp.getString(key, defaultValue);
    }

    public boolean putBoolean(String key, boolean value) {
        return this.sp.edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.sp.getBoolean(key, defaultValue);
    }

    public boolean putInt(String key, int value) {
        return this.sp.edit().putInt(key, value).commit();
    }

    public int getInt(String key, int value) {
        return this.sp.getInt(key, value);
    }
}
