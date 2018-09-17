package com.leisen.wallet.sdk.bean;

import java.util.Locale;

public class CommonRequestParams {
    private String cplc;
    private String funCallId;
    private String serviceId;

    public CommonRequestParams(String serviceId, String funCallId, String cplc) {
        this.serviceId = serviceId;
        this.funCallId = funCallId;
        this.cplc = cplc;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getFunCallId() {
        return this.funCallId;
    }

    public void setFunCallId(String funCallId) {
        this.funCallId = funCallId;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc) {
        this.cplc = cplc;
    }

    public String getSeid() {
        if (this.cplc != null) {
            if (!"".equals(this.cplc) && this.cplc.length() > 42) {
                return new StringBuilder(String.valueOf(this.cplc.substring(0, 4))).append(this.cplc.substring(20, 36)).toString().toUpperCase(Locale.getDefault());
            }
        }
        return null;
    }
}
