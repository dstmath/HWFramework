package com.huawei.wallet.sdk.business.clearssd.request;

import com.huawei.wallet.sdk.common.http.request.RequestBase;

public class RandomRequest extends RequestBase {
    private String cplc;
    private String deviceId;
    private String sign;
    private String signType;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
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

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }
}
