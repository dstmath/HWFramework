package com.android.server.wifi.HwQoE;

import com.android.server.wifi.ABS.HwABSWiFiHandler;

public class HwQoEQualityInfo {
    public int mAPPType = 0;
    public String mBSSID = HwABSWiFiHandler.SUPPLICANT_BSSID_ANY;
    public int mRSSI = 0;
    public long mThoughput = 0;
}
