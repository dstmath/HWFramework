package com.leisen.wallet.sdk.business;

public class GPACOperBusinessForReq extends BaseBusinessForReq {
    private String appAid;
    private int operType;

    public String getAppAid() {
        return this.appAid;
    }

    public void setAppAid(String appAid2) {
        this.appAid = appAid2;
    }

    public int getOperType() {
        return this.operType;
    }

    public void setOperType(int operType2) {
        this.operType = operType2;
    }
}
