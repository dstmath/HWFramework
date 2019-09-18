package com.huawei.wallet.sdk.common.apdu.request;

public class CardStatusQueryRequest extends CardServerBaseRequest {
    public String cplc;
    private String flag;
    public String queryFlag;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getQueryFlag() {
        return this.queryFlag;
    }

    public void setQueryFlag(String queryFlag2) {
        this.queryFlag = queryFlag2;
    }

    public String getFlag() {
        return this.flag;
    }

    public void setFlag(String flag2) {
        this.flag = flag2;
    }
}
