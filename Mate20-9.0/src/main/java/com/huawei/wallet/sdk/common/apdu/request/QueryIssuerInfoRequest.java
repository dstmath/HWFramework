package com.huawei.wallet.sdk.common.apdu.request;

public class QueryIssuerInfoRequest extends CardServerBaseRequest {
    public long timeStamp;

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
    }
}
