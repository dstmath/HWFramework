package com.android.server.am;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;

class GlobalSettingsToPropertiesMapper {
    private static final String TAG = "GlobalSettingsToPropertiesMapper";
    private static final String[][] sGlobalSettingsMapping = {new String[]{"sys_vdso", "sys.vdso"}, new String[]{"fps_divisor", "debug.hwui.fps_divisor"}, new String[]{"display_panel_lpm", "sys.display_panel_lpm"}, new String[]{"sys_uidcpupower", "sys.uidcpupower"}, new String[]{"sys_traced", "sys.traced.enable_override"}};
    private final ContentResolver mContentResolver;
    private final String[][] mGlobalSettingsMapping;

    @VisibleForTesting
    GlobalSettingsToPropertiesMapper(ContentResolver contentResolver, String[][] globalSettingsMapping) {
        this.mContentResolver = contentResolver;
        this.mGlobalSettingsMapping = globalSettingsMapping;
    }

    /* access modifiers changed from: package-private */
    public void updatePropertiesFromGlobalSettings() {
        for (String[] entry : this.mGlobalSettingsMapping) {
            final String settingName = entry[0];
            final String propName = entry[1];
            Uri settingUri = Settings.Global.getUriFor(settingName);
            Preconditions.checkNotNull(settingUri, "Setting " + settingName + " not found");
            ContentObserver co = new ContentObserver(null) {
                public void onChange(boolean selfChange) {
                    GlobalSettingsToPropertiesMapper.this.updatePropertyFromSetting(settingName, propName);
                }
            };
            updatePropertyFromSetting(settingName, propName);
            this.mContentResolver.registerContentObserver(settingUri, false, co);
        }
    }

    public static void start(ContentResolver contentResolver) {
        new GlobalSettingsToPropertiesMapper(contentResolver, sGlobalSettingsMapping).updatePropertiesFromGlobalSettings();
    }

    private String getGlobalSetting(String name) {
        return Settings.Global.getString(this.mContentResolver, name);
    }

    private void setProperty(String key, String value) {
        if (value == null) {
            if (!TextUtils.isEmpty(systemPropertiesGet(key))) {
                value = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            } else {
                return;
            }
        }
        try {
            systemPropertiesSet(key, value);
        } catch (Exception e) {
            if (Build.IS_DEBUGGABLE) {
                Slog.wtf(TAG, "Unable to set property " + key + " value '" + value + "'", e);
            } else {
                Slog.e(TAG, "Unable to set property " + key + " value '" + value + "'", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public String systemPropertiesGet(String key) {
        return SystemProperties.get(key);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void systemPropertiesSet(String key, String value) {
        SystemProperties.set(key, value);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updatePropertyFromSetting(String settingName, String propName) {
        setProperty(propName, getGlobalSetting(settingName));
    }
}
