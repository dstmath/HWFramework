package com.huawei.iconnect.config;

public class ConfigHeader {
    private String fileVersion;
    private String specVersion;

    public ConfigHeader() {
        this.fileVersion = "";
        this.specVersion = "";
    }

    public ConfigHeader(String version, String specversion) {
        this.fileVersion = version;
        this.specVersion = specversion;
    }

    public String getFileVersion() {
        return this.fileVersion;
    }

    public void setFileVersion(String fileVersion) {
        this.fileVersion = fileVersion;
    }

    public String getSpecVersion() {
        return this.specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }
}
