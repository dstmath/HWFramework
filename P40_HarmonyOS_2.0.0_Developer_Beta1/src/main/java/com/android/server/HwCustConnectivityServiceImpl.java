package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

public class HwCustConnectivityServiceImpl extends HwCustConnectivityService {
    private static final boolean IS_SUPPORT_HOTSPOT_DISABLE = SystemProperties.getBoolean("ro.config.support_wifi_hotspot_disable", false);
    private static final int PROVISION_APP_LENGTH = 2;
    private static final String TAG = "HwCustConnectivityServiceImpl";

    public boolean isSupportWifiConnectMode(Context context) {
        if (context == null) {
            return false;
        }
        return "true".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_wifi_connect_mode"));
    }

    public boolean isNeedCustTethering(Context context) {
        String[] appDetails;
        if (!IS_SUPPORT_HOTSPOT_DISABLE || context == null || (appDetails = context.getResources().getStringArray(17236038)) == null || appDetails.length != PROVISION_APP_LENGTH) {
            return false;
        }
        int systemUiUid = -1;
        try {
            systemUiUid = context.getPackageManager().getPackageUidAsUser("com.android.systemui", 1048576, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to resolve SystemUI's UID.");
        }
        if (!(Binder.getCallingUid() == 1000 || Binder.getCallingUid() == systemUiUid)) {
            return true;
        }
        return false;
    }
}
