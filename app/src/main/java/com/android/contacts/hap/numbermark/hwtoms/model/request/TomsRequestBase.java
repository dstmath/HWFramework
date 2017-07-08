package com.android.contacts.hap.numbermark.hwtoms.model.request;

public class TomsRequestBase {
    private String channelno;
    private String imei;
    private String imsi;
    private String queryNum;

    public String getQueryNum() {
        return this.queryNum;
    }

    public void setQueryNum(String queryNum) {
        this.queryNum = queryNum;
    }

    public String getImsi() {
        return this.imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getChannelno() {
        return this.channelno;
    }

    public void setChannelno(String channelno) {
        this.channelno = channelno;
    }
}
