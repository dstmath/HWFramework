package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetworkCallback;
import android.net.wifi.WifiConfiguration;

public interface IHwSupplicantStaNetworkHalEx {
    ISupplicantStaNetworkCallback trySetupNetworkHalForVendorV3_0(WifiConfiguration wifiConfiguration);
}
