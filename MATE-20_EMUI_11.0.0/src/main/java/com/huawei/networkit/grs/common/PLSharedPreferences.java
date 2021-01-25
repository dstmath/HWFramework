package com.huawei.networkit.grs.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Map;

public class PLSharedPreferences {
    private static final String MOVE_TO_DE_RECORDS = "grs_move2DE_records";
    private static final String TAG = "PLSharedPreferences";
    private SharedPreferences sp;

    public PLSharedPreferences(Context context, String spName) {
        this.sp = getSharedPreferences(context, spName);
    }

    private SharedPreferences getSharedPreferences(Context context, String spFileName) {
        if (context == null) {
            Logger.e(TAG, "context is null, must call init method to set context");
            return null;
        }
        Context useContext = context;
        if (Build.VERSION.SDK_INT >= 24) {
            useContext = context.createDeviceProtectedStorageContext();
            SharedPreferences spRecords = useContext.getSharedPreferences(MOVE_TO_DE_RECORDS, 0);
            if (!spRecords.getBoolean(spFileName, false)) {
                if (!useContext.moveSharedPreferencesFrom(context, spFileName)) {
                    useContext = context;
                } else {
                    SharedPreferences.Editor edt = spRecords.edit();
                    edt.putBoolean(spFileName, true);
                    edt.apply();
                }
            }
        }
        return useContext.getSharedPreferences(spFileName, 0);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences == null) {
            return defaultValue;
        }
        return sharedPreferences.getString(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences == null) {
            return defaultValue;
        }
        return sharedPreferences.getLong(key, defaultValue);
    }

    public void putString(String key, String value) {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(key, value).apply();
        }
    }

    public void putLong(String key, long value) {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences != null) {
            sharedPreferences.edit().putLong(key, value).apply();
        }
    }

    public SharedPreferences.Editor edit() {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences == null) {
            return null;
        }
        return sharedPreferences.edit();
    }

    public void removeKeyValue(String key) {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public void clear() {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
    }

    public void remove(String key) {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public Map<String, ?> getAll() {
        SharedPreferences sharedPreferences = this.sp;
        if (sharedPreferences == null) {
            return null;
        }
        return sharedPreferences.getAll();
    }
}
