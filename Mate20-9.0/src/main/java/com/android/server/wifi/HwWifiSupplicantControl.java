package com.android.server.wifi;

import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.LocalLog;

public class HwWifiSupplicantControl extends WifiSupplicantControl {
    private static final String DEFAULT_CERTIFICATE_PATH = (Environment.getDataDirectory().getPath() + "/wapi_certificate");
    public static final String TAG = "HwWifiSupplicantControl";
    private WifiNative mWifiNative;

    HwWifiSupplicantControl(TelephonyManager telephonyManager, WifiNative wifiNative, LocalLog localLog) {
        super(telephonyManager, wifiNative, localLog);
        this.mWifiNative = wifiNative;
    }
}
