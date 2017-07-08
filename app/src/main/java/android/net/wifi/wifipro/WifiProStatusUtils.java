package android.net.wifi.wifipro;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;

public class WifiProStatusUtils {
    public static final String KEY_WIFI_PRO_PROPERTY = "ro.config.hw_wifipro_enable";
    public static final String KEY_WIFI_PRO_SWITCH = "smart_network_switching";

    public static boolean isWifiProEnabledViaProperties() {
        return SystemProperties.getBoolean(KEY_WIFI_PRO_PROPERTY, false);
    }

    public static boolean isWifiProEnabledViaXml(Context context) {
        return context != null && System.getInt(context.getContentResolver(), KEY_WIFI_PRO_SWITCH, 0) == 1;
    }
}
