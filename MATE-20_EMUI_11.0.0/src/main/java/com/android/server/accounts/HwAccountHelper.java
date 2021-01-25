package com.android.server.accounts;

import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.provider.Settings;
import com.huawei.hiai.awareness.AwarenessInnerConstants;

public class HwAccountHelper {
    private static final boolean IS_DEBUG = false;
    private static final String TAG = "HwAccountHelper";

    private HwAccountHelper() {
    }

    private static boolean isPrivacyModeStateOn(Context context) {
        boolean isPrivacyModeOn = true;
        if (!(1 == Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_on", 1) && 1 == Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_state", 0))) {
            isPrivacyModeOn = false;
        }
        return isPrivacyModeOn;
    }

    private static boolean isPrivacyModePkg(String packageName, String account, Context context) {
        String pkgNameList = Settings.Secure.getString(context.getContentResolver(), "privacy_app_list");
        if (pkgNameList == null || "".equals(pkgNameList)) {
            return false;
        }
        String[] pkgNameArray = pkgNameList.contains(AwarenessInnerConstants.SEMI_COLON_KEY) ? pkgNameList.split(AwarenessInnerConstants.SEMI_COLON_KEY) : new String[]{pkgNameList};
        for (String tempName : pkgNameArray) {
            if (tempName.equals(packageName) || tempName.contains(account)) {
                return true;
            }
        }
        return false;
    }

    public static boolean removeProtectAppInPrivacyMode(AuthenticatorDescription desc, boolean isRemoved, Context context) {
        if (!isPrivacyModeStateOn(context) || !isRemoved || !isPrivacyModePkg(desc.packageName, desc.type, context)) {
            return false;
        }
        return true;
    }
}
