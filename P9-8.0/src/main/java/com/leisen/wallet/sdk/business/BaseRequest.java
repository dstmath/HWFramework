package com.leisen.wallet.sdk.business;

public class BaseRequest<T extends Business> {
    private T business;
    private String clientVersion;
    private String cplc;
    private String functionCallId;
    private String imei;
    private String mobileType;
    private String seid;
    private String serviceId;
    private String version;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSeid() {
        return this.seid;
    }

    public void setSeid(String seid) {
        this.seid = seid;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getClientVersion() {
        return this.clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getMobileType() {
        return this.mobileType;
    }

    public void setMobileType(String mobileType) {
        this.mobileType = mobileType;
    }

    public String getFunctionCallId() {
        return this.functionCallId;
    }

    public void setFunctionCallId(String functionCallId) {
        this.functionCallId = functionCallId;
    }

    public T getBusiness() {
        return this.business;
    }

    public void setBusiness(T business) {
        this.business = business;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc) {
        this.cplc = cplc;
    }
}
