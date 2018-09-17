package com.android.server.wifi.util;

import android.telephony.HwTelephonyManager;
import android.util.Log;

public class WifiCommonUtils {
    public static final int CHANNEL_MAX_BAND_2GHZ = 13;
    public static final String DEVICE_WLAN = "WLAN";
    public static final String DEVICE_WLANP2P = "WLAN-P2P";
    public static final String STATE_CONNECT_END = "CONNECT_END";
    public static final String STATE_CONNECT_START = "CONNECT_START";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";
    private static final String TAG = "WifiCommonUtils";

    public static void notifyDeviceState(String device, String state, String extras) {
        if (HwTelephonyManager.getDefault().notifyDeviceState(device, state, "")) {
            Log.i(TAG, "Notify deviceState success. Device = " + device + ", state = " + state);
        } else {
            Log.e(TAG, "Notify deviceState failed. Device = " + device + ", state = " + state);
        }
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
}
