package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.List;

public class WifiConfigStoreUtils {
    private static final int AUTO_CONNECT_DISABLED = 0;
    private static final int AUTO_CONNECT_ENABLED = 1;
    private static final boolean isAutoConnectPropEnabled;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.auto_connect_attwifi", 0) == 1) {
            z = true;
        }
        isAutoConnectPropEnabled = z;
    }

    public static void enableAllNetworks(WifiConfigManager store) {
    }

    public static void loadConfiguredNetworks(WifiConfigManager store) {
        store.loadFromStore();
    }

    public static void disableAllNetworks(WifiConfigManager store) {
    }

    public static void loadAndEnableAllNetworks(WifiConfigManager store) {
    }

    public static List<WifiConfiguration> getConfiguredNetworks(WifiConfigManager store) {
        return store.getSavedNetworks(1010);
    }

    public static boolean isOpenType(WifiConfiguration config) {
        return config != null && config.allowedKeyManagement.cardinality() <= 1 && config.getAuthType() == 0;
    }

    private static WifiConfiguration getSavedWifiConfig(Context context, int networkId) {
        List<WifiConfiguration> savedNetworks;
        if (context == null || networkId == -1 || (savedNetworks = ((WifiManager) context.getSystemService("wifi")).getConfiguredNetworks()) == null) {
            return null;
        }
        for (int i = 0; i < savedNetworks.size(); i++) {
            WifiConfiguration tmp = savedNetworks.get(i);
            if (tmp.networkId == networkId) {
                return tmp;
            }
        }
        return null;
    }

    public static boolean ignoreEnableNetwork(Context context, int networkId, WifiNative wifiNative) {
        WifiConfiguration savedConfig = getSavedWifiConfig(context, networkId);
        if (context == null || savedConfig == null) {
            return false;
        }
        return ignoreEnableNetwork(context, savedConfig, wifiNative);
    }

    public static boolean ignoreEnableNetwork(Context context, WifiConfiguration config, WifiNative wifiNative) {
        if (!HwServiceFactory.getWifiProCommonUtilsEx().hwIsWifiProSwitchOn(context) || config == null) {
            return false;
        }
        if ((!config.noInternetAccess || HwServiceFactory.getWifiProCommonUtilsEx().hwIsAllowWifiConfigRecovery(config.internetHistory)) && (!config.portalNetwork || !isOpenType(config) || config.internetHistory == null || !config.internetHistory.contains("2"))) {
            return false;
        }
        Log.d("WifiConfigStoreUtils", "ignoreEnableNetwork, disable config network in supplicant which has no internet again explicitly netid = " + config.networkId + ", ssid = " + StringUtilEx.safeDisplaySsid(config.SSID));
        return true;
    }

    private static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private static boolean existWifiApInDatabase(Context context, WifiConfiguration config) {
        if (context == null || config == null) {
            return false;
        }
        String data = Settings.System.getString(context.getContentResolver(), "attwifi_hotspot");
        Log.d("WifiConfigStoreUtils", "Database wifiap = " + data);
        boolean isDatabaseWifi = false;
        if (data != null && !"".equals(data)) {
            String[] mAllSpots = data.split(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
            int i = 0;
            while (i < mAllSpots.length && !(isDatabaseWifi = config.SSID.equals(convertToQuotedString(mAllSpots[i])))) {
                i++;
            }
        }
        return isDatabaseWifi;
    }

    private static boolean isAutoConnect(Context context) {
        if (context == null || !isAutoConnectPropEnabled || Settings.System.getInt(context.getContentResolver(), "auto_connect_att", 1) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSkipAutoConnect(Context context, WifiConfiguration config) {
        if (!existWifiApInDatabase(context, config) || isAutoConnect(context)) {
            return false;
        }
        Log.d("WifiConfigStoreUtils", "isSkipAutoConnect ssid = " + StringUtilEx.safeDisplaySsid(config.SSID));
        return true;
    }

    public static boolean skipEnableNetwork(Context context, int networkId, WifiNative wifiNative) {
        WifiConfiguration savedConfig = getSavedWifiConfig(context, networkId);
        if (savedConfig == null || !existWifiApInDatabase(context, savedConfig) || isAutoConnect(context)) {
            return false;
        }
        Log.d("WifiConfigStoreUtils", "skipEnableNetwork ssid = " + StringUtilEx.safeDisplaySsid(savedConfig.SSID));
        return true;
    }
}
