package com.leisen.wallet.sdk.business;

public class GPACOperBusinessForReq extends BaseBusinessForReq {
    private String appAid;
    private int operType;

    public String getAppAid() {
        return this.appAid;
    }

    public void setAppAid(String appAid) {
        this.appAid = appAid;
    }

    public int getOperType() {
        return this.operType;
    }

    public void setOperType(int operType) {
        this.operType = operType;
    }
}
