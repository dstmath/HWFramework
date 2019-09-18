package com.huawei.wallet.sdk.business.buscard.base.spi.model;

import java.io.Serializable;

public class QueryOrder implements Serializable {
    public static final String ORDER_TYPE_CARD_DELETE = "5";
    public static final String ORDER_TYPE_CARD_REPAIR = "6";
    public static final String ORDER_TYPE_CLOUD_TRANSFER_IN = "11";
    public static final String ORDER_TYPE_CLOUD_TRANSFER_OUT = "10";
    public static final String ORDER_TYPE_OPEN_CARD = "0";
    public static final String ORDER_TYPE_OPEN_CARD_AND_RECHARGE = "2";
    public static final String ORDER_TYPE_RECHARGE = "1";
    public static final String ORDER_TYPE_TRANSFER_IN = "4";
    public static final String ORDER_TYPE_TRANSFER_IN_RECHARGE = "7";
    public static final String ORDER_TYPE_TRANSFER_OUT = "3";
    public static final String ORDER_TYPE_VIRTY_CARD = "8";
    public static final String STATUS_BALANCE_CONFIRMATION = "54";
    public static final String STATUS_CREATE_SSD_FAIL = "801";
    public static final String STATUS_DOWNLOAD_CAP_FAIL = "802";
    public static final String STATUS_OPEN_CARD_SUCCESS = "804";
    public static final String STATUS_OTHER = "999";
    public static final String STATUS_PERSONALIZED_FAIL = "803";
    public static final String STATUS_RECHARGE_FAIL = "900";
    public static final String STATUS_RECHARGE_SUCCESS = "902";
    public static final String STATUS_RECHARGE_UNKNOWN = "906";
    public static final String STATUS_REFUNDING = "901";
    public static final String STATUS_REFUND_APPLET = "53";
    public static final String STATUS_REFUND_FAIL = "903";
    public static final String STATUS_REFUND_SUCCESS = "907";
    public static final String STATUS_REMOVEING = "1003";
    public static final String STATUS_REMOVE_DATA_BACKUP = "1002";
    public static final String STATUS_REMOVE_INIT = "1001";
    public static final String STATUS_RESTORE_FINISH = "1102";
    public static final String STATUS_RESTORE_INIT = "1101";
    public static final String STATUS_SNB_ISSUEANDRECHARGE_FAILED = "1001";
    public static final String STATUS_SNB_NORMAL = "1000";
    public static final String STATUS_SNB_PAYED_BUT_FAILED = "1006";
    public static final String STATUS_SNB_RECHARGE_FAILED = "1002";
    public static final String STATUS_SNB_RECHARGE_UNKNOW_FAILED = "1007";
    public static final String STATUS_SNB_REFUNDING = "1004";
    public static final String STATUS_SNB_REFUND_FAILED = "1005";
    public static final String STATUS_SNB_REFUND_SUCCESS = "1008";
    public static final String STATUS_SNB_SHIT_IN_FAILED = "1013";
    public static final String STATUS_SNB_SHIT_IN_RECHARGE_FAILED = "1015";
    public static final String STATUS_SNB_SHIT_OUT_FAILED = "1011";
    public static final String STATUS_TRANSFER_IN_FAILED = "912";
    public static final String STATUS_TRANSFER_IN_RECHARGE_FAILED = "913";
    public static final String STATUS_TRANSFER_OUT_FAILED = "911";
    public static final String STATUS_VIRTUAL_CONSUME_FAILED = "951";
    public static final String STATUS_VIRTUAL_CONSUME_SUCCESS = "950";
    public static final String STATUS_VIRTUAL_HAS_APPLYED = "953";
    public static final String STATUS_VIRTUAL_HAS_PERSONALIZED = "955";
    public static final String STATUS_VIRTUAL_HAS_ROLLBACKED = "956";
    public static final String STATUS_VIRTUAL_IS_RESUMING = "957";
    public static final String STATUS_VIRTUAL_IS_ROLLBACKING = "954";
    public static final String STATUS_VIRTUAL_PERSONALIZED_FAILED = "952";
    private static final long serialVersionUID = 1;
    private String amount = null;
    private String cplc = null;
    private String currency = null;
    private String issuerId = null;
    private String orderId = null;
    private String orderTime = null;
    private String orderType = null;
    private String status = null;

    public void setCurrency(String currency2) {
        this.currency = currency2;
    }

    public String getCurrency() {
        return this.currency;
    }

    public String getOrderTime() {
        return this.orderTime;
    }

    public void setOrderTime(String orderTime2) {
        this.orderTime = orderTime2;
    }

    public String getAmount() {
        return this.amount;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setAmount(String amount2) {
        this.amount = amount2;
    }

    public void setOrderId(String orderId2) {
        this.orderId = orderId2;
    }

    public String getOrderType() {
        return this.orderType;
    }

    public void setOrderType(String orderType2) {
        this.orderType = orderType2;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }
}
