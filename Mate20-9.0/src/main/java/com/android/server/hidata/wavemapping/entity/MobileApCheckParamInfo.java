package com.android.server.hidata.wavemapping.entity;

public class MobileApCheckParamInfo {
    private int mobileApKRecord = 5;
    private int mobileApMinRange = 5;
    private int mobileApMinStd = 5;
    private float mobileApWeightParam = 0.3f;

    public int getMobileApKRecord() {
        return this.mobileApKRecord;
    }

    public void setMobileApKRecord(int mobileApKRecord2) {
        this.mobileApKRecord = mobileApKRecord2;
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
