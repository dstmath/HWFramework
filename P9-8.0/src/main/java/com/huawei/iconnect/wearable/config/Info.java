package com.huawei.iconnect.wearable.config;

public class Info {
    public static final String VERSION_GOOGLE_CHINA = "VERSION_GOOGLE_CHINA";
    public static final String VERSION_OVERSEA = "Oversea";
    private String action = "setup";
    private boolean autoDownload = true;
    private String model = "";
    private boolean needGuide = true;
    private String protocol = "";
    private boolean reconnect = false;
    private String scoAction = "";
    private String vendor = "";
    private String version = "";

    public String getScoAction() {
        return this.scoAction;
    }

    public void setScoAction(String scoAction) {
        this.scoAction = scoAction;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVendor() {
        return this.vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isReconnect() {
        return this.reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    public boolean isAutoDownload() {
        return this.autoDownload;
    }

    public void setAutoDownload(boolean autoDownload) {
        this.autoDownload = autoDownload;
    }

    public boolean isNeedGuide() {
        return this.needGuide;
    }

    public void setNeedGuide(boolean neeGuide) {
        this.needGuide = neeGuide;
    }

    public String toString() {
        return "Info{protocol='" + this.protocol + '\'' + ", vendor='" + this.vendor + '\'' + ", model='" + this.model + '\'' + ", version='" + this.version + '\'' + ", action='" + this.action + '\'' + ", reconnect=" + this.reconnect + ", autoDownload=" + this.autoDownload + ", scoAction=" + this.scoAction + ", need_guide=" + this.needGuide + '}';
    }
}
