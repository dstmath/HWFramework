package com.huawei.wallet.sdk.business.diploma.response;

import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;

public class DiplomaUploadResponse extends CardServerBaseResponse {
    private String returnDesc;

    public String getReturnDesc() {
        return this.returnDesc;
    }

    public void setReturnDesc(String returnDesc2) {
        this.returnDesc = returnDesc2;
    }
}
