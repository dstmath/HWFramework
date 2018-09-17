package com.leisen.wallet.sdk.bean;

public class OperAppletReqParams {
    private String appletAid;
    private String appletVersion;

    public OperAppletReqParams(String appletAid, String appletVersion) {
        this.appletAid = appletAid;
        this.appletVersion = appletVersion;
    }

    public String getAppletAid() {
        return this.appletAid;
    }

    public void setAppletAid(String appletAid) {
        this.appletAid = appletAid;
    }

    public String getAppletVersion() {
        return this.appletVersion;
    }

    public void setAppletVersion(String appletVersion) {
        this.appletVersion = appletVersion;
    }
}
