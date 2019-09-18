package com.huawei.wallet.sdk.business.bankcard.modle;

import java.util.ArrayList;

public class PushCUPOperateMessage {
    public static final String CUP_PUSH_MSG_KEY_CPLC = "cplc";
    public static final String CUP_PUSH_MSG_KEY_TSMLIBDATA = "tsmLibData";
    public static final String CUP_PUSH_MSG_KEY_VIRTUAL_CARD = "virtualCards";
    public static final String CUP_PUSH_MSG_TYPE = "UnionPayPush";
    public static final String TSMLIBDATA_KEY_EVENT = "event";
    public static final String TSMLIBDATA_KEY_SIGN = "sign";
    public static final String TSMLIBDATA_KEY_SSID = "ssid";
    private String cplc;
    private String event;
    private String sign;
    private String ssid;
    private ArrayList<String> virtualCards;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public ArrayList<String> getVirtualCards() {
        return this.virtualCards;
    }

    public void setVirtualCards(ArrayList<String> virtualCards2) {
        this.virtualCards = virtualCards2;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }

    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event2) {
        this.event = event2;
    }
}
