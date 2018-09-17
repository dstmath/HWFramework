package com.leisen.wallet.sdk.business;

public class ApduResBean {
    private String apdu;
    private int index;
    private String sw;

    public ApduResBean(String apdu) {
        this.apdu = apdu;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getApdu() {
        return this.apdu;
    }

    public void setApdu(String apdu) {
        this.apdu = apdu;
    }

    public String getSw() {
        return this.sw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }
}
