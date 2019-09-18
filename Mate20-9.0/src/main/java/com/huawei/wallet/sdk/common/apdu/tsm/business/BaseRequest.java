package com.huawei.wallet.sdk.common.apdu.tsm.business;

import com.huawei.wallet.sdk.common.apdu.tsm.business.Business;

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

    public void setVersion(String version2) {
        this.version = version2;
    }

    public String getSeid() {
        return this.seid;
    }

    public void setSeid(String seid2) {
        this.seid = seid2;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei2) {
        this.imei = imei2;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId2) {
        this.serviceId = serviceId2;
    }

    public String getClientVersion() {
        return this.clientVersion;
    }

    public void setClientVersion(String clientVersion2) {
        this.clientVersion = clientVersion2;
    }

    public String getMobileType() {
        return this.mobileType;
    }

    public void setMobileType(String mobileType2) {
        this.mobileType = mobileType2;
    }

    public String getFunctionCallId() {
        return this.functionCallId;
    }

    public void setFunctionCallId(String functionCallId2) {
        this.functionCallId = functionCallId2;
    }

    public T getBusiness() {
        return this.business;
    }

    public void setBusiness(T business2) {
        this.business = business2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }
}
