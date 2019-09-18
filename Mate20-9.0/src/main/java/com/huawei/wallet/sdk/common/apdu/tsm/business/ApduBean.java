package com.huawei.wallet.sdk.common.apdu.tsm.business;

import java.util.Arrays;

public class ApduBean {
    private String apdu;
    private int index;
    private String[] sw = new String[0];

    public ApduBean() {
    }

    public ApduBean(String apdu2) {
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

    public String[] getSw() {
        return (String[]) Arrays.copyOf(this.sw, this.sw.length);
    }

    public void setSw(String[] sw2) {
        this.sw = (String[]) Arrays.copyOf(sw2, sw2.length);
    }
}
