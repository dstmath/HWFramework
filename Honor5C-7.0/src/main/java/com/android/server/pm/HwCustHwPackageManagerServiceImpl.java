package com.android.server.pm;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.SettingsEx.Systemex;
import android.util.Log;

public class HwCustHwPackageManagerServiceImpl extends HwCustHwPackageManagerService {
    private static final boolean IS_REGIONAL_PHONE_FEATURE;
    private static final String TAG = "HwCustHwPackageManagerServiceImpl";

    static {
        IS_REGIONAL_PHONE_FEATURE = SystemProperties.getBoolean("ro.config.region_phone_feature", IS_REGIONAL_PHONE_FEATURE);
    }

    public boolean isReginalPhoneFeature() {
        return IS_REGIONAL_PHONE_FEATURE;
    }

    public boolean isCustChange(Context context) {
        try {
            String originalVendorCountry = Secure.getString(context.getContentResolver(), "vendor_country");
            String currentVendorCountry = SystemProperties.get("ro.hw.custPath", "");
            if (originalVendorCountry == null) {
                Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                return IS_REGIONAL_PHONE_FEATURE;
            }
            if (!originalVendorCountry.equals(currentVendorCountry)) {
                Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                return true;
            }
            return IS_REGIONAL_PHONE_FEATURE;
        } catch (Exception e) {
            Log.e(TAG, "check cust Exception e : " + e);
        }
    }

    public void changeTheme(String path, Context context) {
        String themePath = Systemex.getString(context.getContentResolver(), "hw_def_theme");
        if (path != null && !path.equals(themePath)) {
            long identity = Binder.clearCallingIdentity();
            Secure.putString(context.getContentResolver(), "isUserChangeTheme", "true");
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean isThemeChange(Context context) {
        return "true".equals(Secure.getString(context.getContentResolver(), "isUserChangeTheme"));
    }
}
