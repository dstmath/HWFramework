package com.android.server.notification;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

public class HwCustZenModeHelperImpl extends HwCustZenModeHelper {
    private static final String NODISTURB_WHITEAPP = "nodisturb_whiteapps";

    public String[] getWhiteApps(Context context) {
        String[] result;
        if (context == null) {
            return null;
        }
        String whiteApps = Settings.Global.getString(context.getContentResolver(), NODISTURB_WHITEAPP);
        if (TextUtils.isEmpty(whiteApps) || (result = whiteApps.split(";")) == null || result.length == 0) {
            return null;
        }
        return result;
    }
}
