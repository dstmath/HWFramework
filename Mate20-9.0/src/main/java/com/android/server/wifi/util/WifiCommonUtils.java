package com.android.server.wifi.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HwWifiServiceFactory;

public class WifiCommonUtils {
    public static final int CHANNEL_MAX_BAND_2GHZ = 13;
    public static final String DEVICE_WLAN = "WLAN";
    public static final String DEVICE_WLANP2P = "WLAN-P2P";
    public static final String STATE_CONNECT_END = "CONNECT_END";
    public static final String STATE_CONNECT_START = "CONNECT_START";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";
    private static final String TAG = "WifiCommonUtils";

    public static void notifyDeviceState(String device, String state, String extras) {
        if (HwWifiServiceFactory.getHwTelphonyUtils().notifyDeviceState(device, state, "")) {
            Log.i(TAG, "Notify deviceState success. Device = " + device + ", state = " + state);
            return;
        }
        Log.e(TAG, "Notify deviceState failed. Device = " + device + ", state = " + state);
    }

    public static int convertFrequencyToChannelNumber(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency < 5170 || frequency > 5825) {
            return 0;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    public static boolean doesNotWifiConnectRejectByCust(WifiConfiguration.NetworkSelectionStatus status, String ssid, Context context) {
        if (context == null) {
            return false;
        }
        String custWifiCureBlackList = Settings.System.getString(context.getContentResolver(), "wifi_cure_black_list");
        String custAutoWifiPackageName = Settings.System.getString(context.getContentResolver(), "cust_auto_wifi_package_name");
        if (TextUtils.isEmpty(custWifiCureBlackList) || TextUtils.isEmpty(custAutoWifiPackageName) || TextUtils.isEmpty(ssid) || ssid.length() < 1 || status == null) {
            return false;
        }
        String compareSsid = ssid.substring(1, ssid.length() - 1);
        String disableName = status.getNetworkSelectionDisableName();
        if (!custWifiCureBlackList.contains(compareSsid) || status.isNetworkEnabled() || disableName == null || !disableName.equals(custAutoWifiPackageName)) {
            return false;
        }
        return true;
    }
}
