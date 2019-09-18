package com.huawei.wallet.sdk.business.idcard.walletbase.tcis.response;

import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.response.CardServerBaseResponse;

public class TcisResponse extends CardServerBaseResponse {
    private String kAInfo;
    private int kaVersion;

    public String getkAInfo() {
        return this.kAInfo;
    }

    public void setkAInfo(String kAInfo2) {
        this.kAInfo = kAInfo2;
    }

    public int getKaVersion() {
        return this.kaVersion;
    }

    public void setKaVersion(int kaVersion2) {
        this.kaVersion = kaVersion2;
    }
}
