package com.huawei.wallet.sdk.business.idcard.walletbase.pass;

import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.response.CardServerBaseResponse;

public class PassTypeIdInfoResponse extends CardServerBaseResponse {
    private boolean enableNFC;
    private String passTypeGroup;
    private String passTypeId;
    private String reserve;

    public String getPassTypeGroup() {
        return this.passTypeGroup;
    }

    public void setPassTypeGroup(String passTypeGroup2) {
        this.passTypeGroup = passTypeGroup2;
    }

    public boolean getEnableNFC() {
        return this.enableNFC;
    }

    public void setEnableNFC(boolean enableNFC2) {
        this.enableNFC = enableNFC2;
    }

    public String getReserve() {
        return this.reserve;
    }

    public void setReserve(String reserve2) {
        this.reserve = reserve2;
    }

    public String getPassTypeId() {
        return this.passTypeId;
    }

    public void setPassTypeId(String passTypeId2) {
        this.passTypeId = passTypeId2;
    }
}
