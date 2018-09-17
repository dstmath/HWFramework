package com.android.server.pm;

import android.content.Context;

public class HwCustHwPackageManagerService {
    public boolean isReginalPhoneFeature() {
        return false;
    }

    public boolean isCustChange(Context context) {
        return false;
    }

    public void changeTheme(String path, Context context) {
    }

    public boolean isThemeChange(Context context) {
        return true;
    }
}
