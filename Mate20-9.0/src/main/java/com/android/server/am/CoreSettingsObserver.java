package com.android.server.am;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.Settings;
import com.android.internal.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;

final class CoreSettingsObserver extends ContentObserver {
    private static final String LOG_TAG = CoreSettingsObserver.class.getSimpleName();
    @VisibleForTesting
    static final Map<String, Class<?>> sGlobalSettingToTypeMap = new HashMap();
    @VisibleForTesting
    static final Map<String, Class<?>> sSecureSettingToTypeMap = new HashMap();
    @VisibleForTesting
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
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    private void sendCoreSettings() {
        populateSettings(this.mCoreSettings, sSecureSettingToTypeMap);
        populateSettings(this.mCoreSettings, sSystemSettingToTypeMap);
        populateSettings(this.mCoreSettings, sGlobalSettingToTypeMap);
        this.mActivityManagerService.onCoreSettingsChange(this.mCoreSettings);
    }

    private void beginObserveCoreSettings() {
        for (String setting : sSecureSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(setting), false, this);
        }
        for (String setting2 : sSystemSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(setting2), false, this);
        }
        for (String setting3 : sGlobalSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(setting3), false, this);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void populateSettings(Bundle snapshot, Map<String, Class<?>> map) {
        String value;
        Context context = this.mActivityManagerService.mContext;
        for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
            String setting = entry.getKey();
            if (map == sSecureSettingToTypeMap) {
                value = Settings.Secure.getString(context.getContentResolver(), setting);
            } else if (map == sSystemSettingToTypeMap) {
                value = Settings.System.getString(context.getContentResolver(), setting);
            } else {
                value = Settings.Global.getString(context.getContentResolver(), setting);
            }
            if (value != null) {
                Class<?> type = entry.getValue();
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
