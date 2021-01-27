package com.android.server.pm;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;

public class HwCustHwPackageManagerServiceImpl extends HwCustHwPackageManagerService {
    private static final boolean IS_COTA_FEATURE = SystemProperties.getBoolean("ro.config.hw_cota", false);
    private static final boolean IS_HOTA_RESTORE_THEME = SystemProperties.getBoolean("ro.config.hw_hotaRestoreTheme", false);
    private static final boolean IS_REGIONAL_PHONE_FEATURE = SystemProperties.getBoolean("ro.config.region_phone_feature", false);
    private static final String TAG = "HwCustHwPackageManagerServiceImpl";

    public boolean isReginalPhoneFeature() {
        return IS_REGIONAL_PHONE_FEATURE;
    }

    public boolean isSupportThemeRestore() {
        return IS_REGIONAL_PHONE_FEATURE || IS_COTA_FEATURE || IS_HOTA_RESTORE_THEME;
    }

    public void changeTheme(String path, Context context) {
        String themePath = SettingsEx.Systemex.getString(context.getContentResolver(), "hw_def_theme");
        if (path != null && !path.equals(themePath)) {
            long identity = Binder.clearCallingIdentity();
            Settings.Secure.putString(context.getContentResolver(), "isUserChangeTheme", "true");
            Binder.restoreCallingIdentity(identity);
        }
    }
}
