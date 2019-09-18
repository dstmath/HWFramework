package com.huawei.wallet.sdk.business.bankcard.response;

import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;

public class QueryAidResponse extends CardServerBaseResponse {
    public static final int RESPONSE_CODE_AID_NOT_EXISTED = -3;
    public static final int RESPONSE_CODE_CARD_UNSTARTED_OR_DELETEED = -5;
    private String aid;
    private String virtualCardRefID;

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public String getVirtualCardRefID() {
        return this.virtualCardRefID;
    }

    public void setVirtualCardRefID(String virtualCardRefID2) {
        this.virtualCardRefID = virtualCardRefID2;
    }
}
