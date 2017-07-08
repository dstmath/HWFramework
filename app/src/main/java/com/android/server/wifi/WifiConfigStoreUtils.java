package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.net.wifi.wifipro.WifiProStatusUtils;
import android.util.Log;
import java.util.List;

public class WifiConfigStoreUtils {
    public static void enableAllNetworks(WifiConfigManager store) {
        store.enableAllNetworks();
    }

    public static void loadConfiguredNetworks(WifiConfigManager store) {
        store.loadConfiguredNetworks();
    }

    public static void disableAllNetworks(WifiConfigManager store) {
        store.disableAllNetworksNative();
    }

    public static void loadAndEnableAllNetworks(WifiConfigManager store) {
        store.loadAndEnableAllNetworks();
    }

    public static boolean disableNetwork(WifiConfigManager store, int netId, int reason) {
        return store.disableNetwork(netId);
    }

    public static List<WifiConfiguration> getConfiguredNetworks(WifiConfigManager store) {
        return store.getSavedNetworks();
    }

    public static boolean isOpenType(WifiConfiguration config) {
        boolean z = true;
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        if (config.getAuthType() != 0) {
            z = false;
        }
        return z;
    }

    public static boolean ignoreEnableNetwork(Context context, int networkId, WifiNative wifiNative) {
        WifiConfiguration config = null;
        if (!(context == null || networkId == -1)) {
            List<WifiConfiguration> savedNetworks = ((WifiManager) context.getSystemService("wifi")).getConfiguredNetworks();
            if (savedNetworks != null) {
                for (int i = 0; i < savedNetworks.size(); i++) {
                    WifiConfiguration tmp = (WifiConfiguration) savedNetworks.get(i);
                    if (tmp.networkId == networkId) {
                        config = tmp;
                        break;
                    }
                }
            }
        }
        return ignoreEnableNetwork(context, config, wifiNative);
    }

    public static boolean ignoreEnableNetwork(Context context, WifiConfiguration config, WifiNative wifiNative) {
        if (!WifiProStatusUtils.isWifiProEnabledViaXml(context) || config == null || ((!config.noInternetAccess || NetworkHistoryUtils.allowWifiConfigRecovery(config.internetHistory)) && (!config.portalNetwork || !isOpenType(config) || config.internetHistory == null || !config.internetHistory.contains("2")))) {
            return false;
        }
        Log.d("WifiConfigStoreUtils", "ignoreEnableNetwork, disable config network in supplicant which has no internet again explicitly, netid = " + config.networkId + ", ssid = " + config.SSID);
        wifiNative.disableNetwork(config.networkId);
        return true;
    }
}
