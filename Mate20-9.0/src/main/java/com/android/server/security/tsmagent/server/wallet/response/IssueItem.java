package com.android.server.security.tsmagent.server.wallet.response;

public class IssueItem {
    private String apkname;
    private String appSign;
    private String appletAid;
    private String ssdAid;
    private int status;

    public String getApkname() {
        return this.apkname;
    }

    public void setApkname(String apkname2) {
        this.apkname = apkname2;
    }

    public String getSsdAid() {
        return this.ssdAid;
    }

    public void setSsdAid(String ssdAid2) {
        this.ssdAid = ssdAid2;
    }

    public String getAppletAid() {
        return this.appletAid;
    }

    public void setAppletAid(String appletAid2) {
        this.appletAid = appletAid2;
    }

    public String getAppSign() {
        return this.appSign;
    }

    public void setAppSign(String appSign2) {
        this.appSign = appSign2;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status2) {
        this.status = status2;
    }
}
