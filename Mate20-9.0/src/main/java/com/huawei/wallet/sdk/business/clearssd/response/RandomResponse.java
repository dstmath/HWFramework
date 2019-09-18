package com.huawei.wallet.sdk.business.clearssd.response;

import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;

public class RandomResponse extends CardServerBaseResponse {
    private String rand;

    public String getRand() {
        return this.rand;
    }

    public void setRand(String rand2) {
        this.rand = rand2;
    }
}
