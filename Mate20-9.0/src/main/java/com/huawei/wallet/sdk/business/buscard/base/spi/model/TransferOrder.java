package com.huawei.wallet.sdk.business.buscard.base.spi.model;

public class TransferOrder {
    public static final String STATUS_BACKUP = "1002";
    public static final String STATUS_TRANSFERING_IN = "1101";
    public static final String STATUS_TRANSFERING_OUT = "1001";
    public static final String STATUS_TRANSFER_IN_DONE = "1102";
    public static final String STATUS_TRANSFER_OUT_DONE = "1003";
    public static final int TYPE_IN = 11;
    public static final int TYPE_OUT = 10;
    private String bankCardApplet;
    private String mCPLC;
    private String mCardNum;
    private String mDateTime;
    private String mOrderNum;
    private String mOrderStatus;
    private String mOrderType;
    private String mUserId;

    public String getmCardNum() {
        return this.mCardNum;
    }

    public void setmCardNum(String mCardNum2) {
        this.mCardNum = mCardNum2;
    }

    public String getOrderNum() {
        return this.mOrderNum;
    }

    public void setOrderNum(String mOrderNum2) {
        this.mOrderNum = mOrderNum2;
    }

    public String getOrderType() {
        return this.mOrderType;
    }

    public void setOrderType(String mOrderType2) {
        this.mOrderType = mOrderType2;
    }

    public String getDateTime() {
        return this.mDateTime;
    }

    public void setDateTime(String mDateTime2) {
        this.mDateTime = mDateTime2;
    }

    public String getUserId() {
        return this.mUserId;
    }

    public void setUserId(String mUserId2) {
        this.mUserId = mUserId2;
    }

    public String getmCPLC() {
        return this.mCPLC;
    }

    public void setCplc(String mCPLC2) {
        this.mCPLC = mCPLC2;
    }

    public String getAppletType() {
        return this.bankCardApplet;
    }

    public void setAppletType(String appletTppe) {
        this.bankCardApplet = appletTppe;
    }

    public String getOrderStatus() {
        return this.mOrderStatus;
    }

    public void setOrderStatus(String mOrderStatus2) {
        this.mOrderStatus = mOrderStatus2;
    }
}
