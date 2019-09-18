package com.leisen.wallet.sdk.business;

public class AppletOperBusinessForReq extends BaseBusinessForReq {
    private String appAid;
    private String appletVersion;
    private int operType;

    public String getAppAid() {
        return this.appAid;
    }

    public void setAppAid(String appAid2) {
        this.appAid = appAid2;
    }

    public String getAppletVersion() {
        return this.appletVersion;
    }

    public void setAppletVersion(String appletVersion2) {
        this.appletVersion = appletVersion2;
    }

    public int getOperType() {
        return this.operType;
    }

    public void setOperType(int operType2) {
        this.operType = operType2;
    }
}
