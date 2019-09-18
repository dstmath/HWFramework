package com.leisen.wallet.sdk.bean;

import java.util.Locale;

public class CommonRequestParams {
    private String cplc;
    private String funCallId;
    private String serviceId;

    public CommonRequestParams(String serviceId2, String funCallId2, String cplc2) {
        this.serviceId = serviceId2;
        this.funCallId = funCallId2;
        this.cplc = cplc2;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId2) {
        this.serviceId = serviceId2;
    }

    public String getFunCallId() {
        return this.funCallId;
    }

    public void setFunCallId(String funCallId2) {
        this.funCallId = funCallId2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getSeid() {
        if (this.cplc == null || "".equals(this.cplc) || this.cplc.length() <= 42) {
            return null;
        }
        return (this.cplc.substring(0, 4) + this.cplc.substring(20, 36)).toUpperCase(Locale.getDefault());
    }
}
