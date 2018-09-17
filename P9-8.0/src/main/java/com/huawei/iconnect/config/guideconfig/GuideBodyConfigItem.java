package com.huawei.iconnect.config.guideconfig;

import java.util.HashMap;
import java.util.List;

public class GuideBodyConfigItem {
    private static final int DEFAULT_INFO_ID = -1;
    private HashMap<String, String> actionList;
    private HashMap<String, String> apkName;
    private String appid = "";
    private int infoTextId = -1;
    private String infoUrl = "";
    private List<String> modelList;
    private HashMap<String, Object> params;
    private String protocol = "";
    private String vendor = "";
    private String version = "";

    public int getInfoTextId() {
        return this.infoTextId;
    }

    public void setInfoTextId(int infoTextId) {
        this.infoTextId = infoTextId;
    }

    public HashMap<String, String> getActionList() {
        return this.actionList;
    }

    public void setActionList(HashMap<String, String> actionList) {
        this.actionList = actionList;
    }

    public HashMap<String, String> getApkName() {
        return this.apkName;
    }

    public void setApkName(HashMap<String, String> apkName) {
        this.apkName = apkName;
    }

    public List<String> getModelList() {
        return this.modelList;
    }

    public void setModelList(List<String> modelList) {
        this.modelList = modelList;
    }

    public String getInfoUrl() {
        return this.infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
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

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppid() {
        return this.appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public HashMap<String, Object> getParams() {
        return this.params;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }
}
