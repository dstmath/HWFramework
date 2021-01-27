package com.huawei.hwwifiproservice;

import java.util.ArrayList;
import java.util.List;

public class WifiProDualBandApInfoRcd {
    private static final String TAG = "WifiProDualBandApInfoRcd";
    public Short mApAuthType;
    public String mApBSSID;
    public String mApSSID;
    public int mChannelFrequency;
    public int mDisappearCount;
    public int mInBlackList;
    public Short mInetCapability;
    private List<WifiProRelateApRcd> mRelateApRcds = new ArrayList();
    public Short mServingBand;
    public long mUpdateTime;

    public WifiProDualBandApInfoRcd(String bssid) {
        resetAllParameters(bssid);
    }

    private void resetAllParameters(String bssid) {
        this.mApBSSID = "DEAULT_STR";
        if (bssid != null) {
            this.mApBSSID = bssid;
        }
        this.mApSSID = "DEAULT_STR";
        this.mInetCapability = 0;
        this.mServingBand = 0;
        this.mApAuthType = 0;
        this.mDisappearCount = 0;
        this.mInBlackList = 0;
        this.mChannelFrequency = 0;
        this.mUpdateTime = 0;
    }

    public List<WifiProRelateApRcd> getRelateApRcds() {
        return this.mRelateApRcds;
    }

    public void setRelateApRcds(List<WifiProRelateApRcd> relateApRcds) {
        this.mRelateApRcds = relateApRcds;
    }
}
