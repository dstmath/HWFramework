package com.huawei.hwwifiproservice;

public class WifiProRelateApRcd {
    public static final int RCD_DOUBLE_BYTE_LEN = 568;
    private static final String TAG = "WifiProRelateApRcd";
    public String mApBSSID;
    public int mMaxCurrentRSSI;
    public int mMaxRelatedRSSI;
    public int mMinCurrentRSSI;
    public int mMinRelatedRSSI;
    public int mRelateType;
    public String mRelatedBSSID;

    public WifiProRelateApRcd(String bssid) {
        resetAllParameters(bssid);
    }

    private void resetAllParameters(String bssid) {
        this.mApBSSID = "DEAULT_STR";
        if (bssid != null) {
            this.mApBSSID = bssid;
        }
        this.mRelatedBSSID = "DEAULT_STR";
        this.mRelateType = 0;
        this.mMaxCurrentRSSI = 0;
        this.mMaxRelatedRSSI = 0;
        this.mMinCurrentRSSI = 0;
        this.mMinRelatedRSSI = 0;
    }
}
