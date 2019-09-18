package com.leisen.wallet.sdk.bean;

public class OperAppletReqParams {
    private String appletAid;
    private String appletVersion;

    public OperAppletReqParams(String appletAid2, String appletVersion2) {
        this.appletAid = appletAid2;
        this.appletVersion = appletVersion2;
    }

    public String getAppletAid() {
        return this.appletAid;
    }

    public void setAppletAid(String appletAid2) {
        this.appletAid = appletAid2;
    }

    public String getAppletVersion() {
        return this.appletVersion;
    }

    public void setAppletVersion(String appletVersion2) {
        this.appletVersion = appletVersion2;
    }
}
