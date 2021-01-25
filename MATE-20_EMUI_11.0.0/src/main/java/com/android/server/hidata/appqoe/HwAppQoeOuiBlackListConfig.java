package com.android.server.hidata.appqoe;

public class HwAppQoeOuiBlackListConfig {
    private int mFeatureId = -1;
    private String mOuiName = HwAPPQoEUtils.INVALID_STRING_VALUE;

    public void setOuiName(String ouiName) {
        this.mOuiName = ouiName;
    }

    public void setFeatureId(int featureId) {
        this.mFeatureId = featureId;
    }

    public String getOuiName() {
        return this.mOuiName;
    }

    public int getFeatureId() {
        return this.mFeatureId;
    }
}
