package com.android.server.security.tsmagent.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class NFCPreferences {
    private static final String HOTALK_PROPERTIES_NAME = "NFC_Properties";
    private static NFCPreferences instance = null;
    private static final Object lock = new Object();
    private SharedPreferences sharedPreferences = null;

    private NFCPreferences(Context context) {
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(HOTALK_PROPERTIES_NAME, 0);
    }

    public static NFCPreferences getInstance(Context context) {
        synchronized (lock) {
            if (instance == null) {
                instance = new NFCPreferences(context);
            }
        }
        return instance;
    }

    public boolean putString(String key, String value) {
        return this.sharedPreferences.edit().putString(key, value).commit();
    }

    public String getString(String key, String defaultValue) {
        return this.sharedPreferences.getString(key, defaultValue);
    }
}
