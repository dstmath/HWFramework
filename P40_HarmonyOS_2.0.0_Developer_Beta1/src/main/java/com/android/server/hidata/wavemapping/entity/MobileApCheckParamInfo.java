package com.android.server.hidata.wavemapping.entity;

public class MobileApCheckParamInfo {
    private static final int DEFAULT_VALUE = 5;
    private static final float DEFAULT_WEIGHT_PARAM = 0.3f;
    private int mobileApMinRange = 5;
    private int mobileApMinStd = 5;
    private float mobileApWeightParam = DEFAULT_WEIGHT_PARAM;
    private int mobileApkRecord = 5;

    public int getMobileApkRecord() {
        return this.mobileApkRecord;
    }

    public void setMobileApkRecord(int mobileApkRecord2) {
        this.mobileApkRecord = mobileApkRecord2;
    }

    public int getMobileApMinStd() {
        return this.mobileApMinStd;
    }

    public void setMobileApMinStd(int mobileApMinStd2) {
        this.mobileApMinStd = mobileApMinStd2;
    }

    public int getMobileApMinRange() {
        return this.mobileApMinRange;
    }

    public void setMobileApMinRange(int mobileApMinRange2) {
        this.mobileApMinRange = mobileApMinRange2;
    }

    public float getMobileApWeightParam() {
        return this.mobileApWeightParam;
    }

    public void setMobileApWeightParam(float mobileApWeightParam2) {
        this.mobileApWeightParam = mobileApWeightParam2;
    }
}
