package com.huawei.wallet.sdk.business.bankcard.request;

import com.huawei.wallet.sdk.common.apdu.request.CardServerBaseRequest;

public class QueryUnionPayPushRequest extends CardServerBaseRequest {
    private String cardRefId;
    public String cplc;
    private String queryFlag;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getCardRefId() {
        return this.cardRefId;
    }

    public void setCardRefId(String cardRefId2) {
        this.cardRefId = cardRefId2;
    }

    public String getQueryFlag() {
        return this.queryFlag;
    }

    public void setQueryFlag(String queryFlag2) {
        this.queryFlag = queryFlag2;
    }
}
