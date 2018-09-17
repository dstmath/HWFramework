package com.android.server.notification;

import android.content.Context;
import android.provider.Settings.Global;
import android.text.TextUtils;

public class HwCustZenModeHelperImpl extends HwCustZenModeHelper {
    private static final String NODISTURB_WHITEAPP = "nodisturb_whiteapps";

    public String[] getWhiteApps(Context context) {
        String whiteApps = Global.getString(context.getContentResolver(), NODISTURB_WHITEAPP);
        if (TextUtils.isEmpty(whiteApps)) {
            return null;
        }
        String[] result = whiteApps.split(";");
        if (result == null || result.length == 0) {
            return null;
        }
        return result;
    }
}
