package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.request;

public class ServerAccessBaseRequest extends CardServerBaseRequest {
    public static final String PAY_TYPE_HUAWEIPAY = "Huaweipay";
    private String appletAid = null;
    private String cardId = null;
    private String cplc = null;
    private String deviceModel = null;
    private String issuerId = null;
    private String orderId = null;
    private String payType = "Huaweipay";
    private String phoneManufacturer = null;
    private String phoneNumber = null;
    private int requestTimes = 0;
    private String reserved = null;
    private String seChipManuFacturer = null;
    private String sn = null;
    private String type;
    private String userId = null;

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }

    public void setSeChipManuFacturer(String seChipManuFacturer2) {
        this.seChipManuFacturer = seChipManuFacturer2;
    }

    public String getSeChipManuFacturer() {
        return this.seChipManuFacturer;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setDeviceModel(String deviceModel2) {
        this.deviceModel = deviceModel2;
    }

    public String getPayType() {
        return this.payType;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setPayType(String payType2) {
        this.payType = payType2;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber2) {
        this.phoneNumber = phoneNumber2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getAppletAid() {
        return this.appletAid;
    }

    public void setAppletAid(String appletAid2) {
        this.appletAid = appletAid2;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn2) {
        this.sn = sn2;
    }

    public String getPhoneManufacturer() {
        return this.phoneManufacturer;
    }

    public void setPhoneManufacturer(String phoneManufacturer2) {
        this.phoneManufacturer = phoneManufacturer2;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId2) {
        this.cardId = cardId2;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId2) {
        this.orderId = orderId2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public void setRequestTimes(int times) {
        this.requestTimes = times;
    }

    public int getRequestTimes() {
        return this.requestTimes;
    }
}
