package com.android.server.notification;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

public class HwCustDefaultApprovedAppsImpl extends HwCustDefaultApprovedApps {
    private static final String DEFAULTAPPROVIED_WHITEAPPS = "default_approvied_whiteapps";
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");

    public String getWhiteApps(Context context) {
        if (!IS_DOCOMO) {
            return null;
        }
        return Settings.Global.getString(context.getContentResolver(), DEFAULTAPPROVIED_WHITEAPPS);
    }
}
