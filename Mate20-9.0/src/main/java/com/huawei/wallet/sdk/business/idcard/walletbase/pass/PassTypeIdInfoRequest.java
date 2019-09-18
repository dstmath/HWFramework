package com.huawei.wallet.sdk.business.idcard.walletbase.pass;

import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.request.BaseLibCardServerBaseRequest;

public class PassTypeIdInfoRequest extends BaseLibCardServerBaseRequest {
    private String appid;
    private String passTypeId;

    public String getAppId() {
        return this.appid;
    }

    public void setAppId(String appid2) {
        this.appid = appid2;
    }

    public String getPassTypeId() {
        return this.passTypeId;
    }

    public void setPassTypeId(String passTypeId2) {
        this.passTypeId = passTypeId2;
    }
}
