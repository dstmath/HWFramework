package com.android.server.am;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

final class CoreSettingsObserver extends ContentObserver {
    private static final String LOG_TAG = CoreSettingsObserver.class.getSimpleName();
    static final Map<String, Class<?>> sGlobalSettingToTypeMap = new HashMap();
    static final Map<String, Class<?>> sSecureSettingToTypeMap = new HashMap();
    static final Map<String, Class<?>> sSystemSettingToTypeMap = new HashMap();
    private final ActivityManagerService mActivityManagerService;
    private final Bundle mCoreSettings = new Bundle();

    static {
        sSecureSettingToTypeMap.put("long_press_timeout", Integer.TYPE);
        sSecureSettingToTypeMap.put("multi_press_timeout", Integer.TYPE);
        sSystemSettingToTypeMap.put("time_12_24", String.class);
        sGlobalSettingToTypeMap.put("debug_view_attributes", Integer.TYPE);
    }

    public CoreSettingsObserver(ActivityManagerService activityManagerService) {
        super(activityManagerService.mHandler);
        this.mActivityManagerService = activityManagerService;
        beginObserveCoreSettings();
        sendCoreSettings();
    }

    public Bundle getCoreSettingsLocked() {
        return (Bundle) this.mCoreSettings.clone();
    }

    public void onChange(boolean selfChange) {
        synchronized (this.mActivityManagerService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                sendCoreSettings();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void sendCoreSettings() {
        populateSettings(this.mCoreSettings, sSecureSettingToTypeMap);
        populateSettings(this.mCoreSettings, sSystemSettingToTypeMap);
        populateSettings(this.mCoreSettings, sGlobalSettingToTypeMap);
        this.mActivityManagerService.onCoreSettingsChange(this.mCoreSettings);
    }

    private void beginObserveCoreSettings() {
        for (String setting : sSecureSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(setting), false, this);
        }
        for (String setting2 : sSystemSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(System.getUriFor(setting2), false, this);
        }
        for (String setting22 : sGlobalSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Global.getUriFor(setting22), false, this);
        }
    }

    void populateSettings(Bundle snapshot, Map<String, Class<?>> map) {
        Context context = this.mActivityManagerService.mContext;
        for (Entry<String, Class<?>> entry : map.entrySet()) {
            String value;
            String setting = (String) entry.getKey();
            if (map == sSecureSettingToTypeMap) {
                value = Secure.getString(context.getContentResolver(), setting);
            } else if (map == sSystemSettingToTypeMap) {
                value = System.getString(context.getContentResolver(), setting);
            } else {
                value = Global.getString(context.getContentResolver(), setting);
            }
            if (value != null) {
                Class<?> type = (Class) entry.getValue();
                if (type == String.class) {
                    snapshot.putString(setting, value);
                } else if (type == Integer.TYPE) {
                    snapshot.putInt(setting, Integer.parseInt(value));
                } else if (type == Float.TYPE) {
                    snapshot.putFloat(setting, Float.parseFloat(value));
                } else if (type == Long.TYPE) {
                    snapshot.putLong(setting, Long.parseLong(value));
                }
            }
        }
    }
}
