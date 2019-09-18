package com.huawei.wallet.sdk.business.bankcard.request;

import com.huawei.wallet.sdk.common.apdu.request.CardServerBaseRequest;

public class WipeAllBankCardRequest extends CardServerBaseRequest {
    public static final String WIPE_ALL_CUP_CARD = "10";
    private int brand;
    private String cplc;
    private String event;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event2) {
        this.event = event2;
    }

    public int getBrand() {
        return this.brand;
    }

    public void setBrand(int brand2) {
        this.brand = brand2;
    }
}
