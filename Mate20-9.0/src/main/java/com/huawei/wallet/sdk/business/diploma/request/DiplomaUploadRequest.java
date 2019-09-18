package com.huawei.wallet.sdk.business.diploma.request;

import com.huawei.wallet.sdk.common.http.request.RequestBase;

public class DiplomaUploadRequest extends RequestBase {
    private String businessCert;
    private String cplcList;
    private String deviceCert;
    private String deviceId;
    private String sign;
    private String signType;

    public String getDeviceCert() {
        return this.deviceCert;
    }

    public void setDeviceCert(String deviceCert2) {
        this.deviceCert = deviceCert2;
    }

    public String getBusinessCert() {
        return this.businessCert;
    }

    public void setBusinessCert(String businessCert2) {
        this.businessCert = businessCert2;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }

    public String getSignType() {
        return this.signType;
    }

    public void setSignType(String signType2) {
        this.signType = signType2;
    }

    public String getCplcList() {
        return this.cplcList;
    }

    public void setCplcList(String cplcList2) {
        this.cplcList = cplcList2;
    }
}
