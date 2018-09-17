package com.android.server.pm;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.SettingsEx.Systemex;

public class HwCustHwPackageManagerServiceImpl extends HwCustHwPackageManagerService {
    private static final boolean IS_COTA_FEATURE = SystemProperties.getBoolean("ro.config.hw_cota", false);
    private static final boolean IS_REGIONAL_PHONE_FEATURE = SystemProperties.getBoolean("ro.config.region_phone_feature", false);
    private static final String TAG = "HwCustHwPackageManagerServiceImpl";

    public boolean isReginalPhoneFeature() {
        return IS_REGIONAL_PHONE_FEATURE;
    }

    public boolean isSupportThemeRestore() {
        return !IS_REGIONAL_PHONE_FEATURE ? IS_COTA_FEATURE : true;
    }

    public void changeTheme(String path, Context context) {
        String themePath = Systemex.getString(context.getContentResolver(), "hw_def_theme");
        if (path != null && (path.equals(themePath) ^ 1) != 0) {
            long identity = Binder.clearCallingIdentity();
            Secure.putString(context.getContentResolver(), "isUserChangeTheme", "true");
            Binder.restoreCallingIdentity(identity);
        }
    }
}
