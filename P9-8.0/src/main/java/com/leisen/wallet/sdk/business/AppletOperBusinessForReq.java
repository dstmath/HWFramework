package com.leisen.wallet.sdk.business;

public class AppletOperBusinessForReq extends BaseBusinessForReq {
    private String appAid;
    private String appletVersion;
    private int operType;

    public String getAppAid() {
        return this.appAid;
    }

    public void setAppAid(String appAid) {
        this.appAid = appAid;
    }

    public String getAppletVersion() {
        return this.appletVersion;
    }

    public void setAppletVersion(String appletVersion) {
        this.appletVersion = appletVersion;
    }

    public int getOperType() {
        return this.operType;
    }

    public void setOperType(int operType) {
        this.operType = operType;
    }
}
