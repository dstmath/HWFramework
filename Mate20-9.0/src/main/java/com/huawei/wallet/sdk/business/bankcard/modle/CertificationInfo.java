package com.huawei.wallet.sdk.business.bankcard.modle;

public class CertificationInfo {
    private String authSignResult;
    private String deviceCert;
    private String serviceCert;

    public String getDeviceCert() {
        return this.deviceCert;
    }

    public void setDeviceCert(String deviceCertValue) {
        this.deviceCert = deviceCertValue;
    }

    public String getServiceCert() {
        return this.serviceCert;
    }

    public void setServiceCert(String serviceCertValue) {
        this.serviceCert = serviceCertValue;
    }

    public String getAuthSignResult() {
        return this.authSignResult;
    }

    public void setAuthSignResult(String authSignResultValue) {
        this.authSignResult = authSignResultValue;
    }
}
