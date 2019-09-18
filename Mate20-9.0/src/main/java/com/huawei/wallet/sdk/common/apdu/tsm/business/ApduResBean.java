package com.huawei.wallet.sdk.common.apdu.tsm.business;

public class ApduResBean {
    private String apdu;
    private int index;
    private String sw;

    public ApduResBean() {
    }

    public ApduResBean(String apdu2) {
        this.apdu = apdu2;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index2) {
        this.index = index2;
    }

    public String getApdu() {
        return this.apdu;
    }

    public void setApdu(String apdu2) {
        this.apdu = apdu2;
    }

    public String getSw() {
        return this.sw;
    }

    public void setSw(String sw2) {
        this.sw = sw2;
    }
}
