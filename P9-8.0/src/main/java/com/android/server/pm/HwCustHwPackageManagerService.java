package com.android.server.pm;

import android.content.Context;

public class HwCustHwPackageManagerService {
    public boolean isReginalPhoneFeature() {
        return false;
    }

    public boolean isSupportThemeRestore() {
        return false;
    }

    public void changeTheme(String path, Context context) {
    }
}
