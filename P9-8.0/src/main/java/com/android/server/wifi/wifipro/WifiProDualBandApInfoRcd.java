package com.android.server.wifi.wifipro;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class WifiProDualBandApInfoRcd {
    private static final String TAG = "WifiProDualBandApInfoRcd";
    public String apBSSID;
    public int isInBlackList;
    public Short mApAuthType;
    public String mApSSID;
    public int mChannelFrequency;
    public int mDisappearCount;
    public Short mInetCapability;
    private List<WifiProRelateApRcd> mRelateApRcds = new ArrayList();
    public Short mServingBand;
    public long mUpdateTime;

    public WifiProDualBandApInfoRcd(String bssid) {
        resetAllParameters(bssid);
    }

    private void resetAllParameters(String bssid) {
        this.apBSSID = "DEAULT_STR";
        if (bssid != null) {
            this.apBSSID = bssid;
        }
        this.mApSSID = "DEAULT_STR";
        this.mInetCapability = Short.valueOf((short) 0);
        this.mServingBand = Short.valueOf((short) 0);
        this.mApAuthType = Short.valueOf((short) 0);
        this.mDisappearCount = 0;
        this.isInBlackList = 0;
        this.mChannelFrequency = 0;
        this.mUpdateTime = 0;
    }

    public List<WifiProRelateApRcd> getRelateApRcds() {
        return this.mRelateApRcds;
    }

    public void setRelateApRcds(List<WifiProRelateApRcd> relateApRcds) {
        this.mRelateApRcds = relateApRcds;
    }

    public void dumpAll() {
        Log.d(TAG, "apBSSID:" + this.apBSSID + ", mApSSID:" + this.mApSSID + ", mInetCapability:" + this.mInetCapability + ", mServingBand:" + this.mServingBand + ", mApAuthType:" + this.mApAuthType + ", mChannelFrequency:" + this.mChannelFrequency + ", mDisappearCount:" + this.mDisappearCount + ", isInBlackList:" + this.isInBlackList + ", mUpdateTime:" + this.mUpdateTime);
    }
}
