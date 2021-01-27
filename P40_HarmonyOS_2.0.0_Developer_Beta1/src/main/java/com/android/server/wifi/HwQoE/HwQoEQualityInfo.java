package com.android.server.wifi.HwQoE;

import com.android.server.wifi.ABS.HwAbsWiFiHandler;

public class HwQoEQualityInfo {
    public int mAPPType = 0;
    public String mBSSID = HwAbsWiFiHandler.SUPPLICANT_BSSID_ANY;
    public int mRSSI = 0;
    public long mThoughput = 0;
}
