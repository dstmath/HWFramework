package com.huawei.wallet.sdk.business.buscard.model;

public class TransferEventBase {
    private String cityCode;
    private String deviceType;
    private String eventId;
    private String issuerId;
    private String newCardNumber;
    private String newCplc;
    private String newTerminal;
    private String oldCardNumber;
    private String oldCplc;
    private String oldTerminal;
    private String status;
    private String userId;

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId2) {
        this.eventId = eventId2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public String getOldCardNumber() {
        return this.oldCardNumber;
    }

    public void setOldCardNumber(String oldCardNumber2) {
        this.oldCardNumber = oldCardNumber2;
    }

    public String getNewCardNumber() {
        return this.newCardNumber;
    }

    public void setNewCardNumber(String newCardNumber2) {
        this.newCardNumber = newCardNumber2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(String deviceType2) {
        this.deviceType = deviceType2;
    }

    public String getOldCplc() {
        return this.oldCplc;
    }

    public void setOldCplc(String oldCplc2) {
        this.oldCplc = oldCplc2;
    }

    public String getOldTerminal() {
        return this.oldTerminal;
    }

    public void setOldTerminal(String oldTerminal2) {
        this.oldTerminal = oldTerminal2;
    }

    public String getNewCplc() {
        return this.newCplc;
    }

    public void setNewCplc(String newCplc2) {
        this.newCplc = newCplc2;
    }

    public String getNewTerminal() {
        return this.newTerminal;
    }

    public void setNewTerminal(String newTerminal2) {
        this.newTerminal = newTerminal2;
    }

    public String getCityCode() {
        return this.cityCode;
    }

    public void setCityCode(String cityCode2) {
        this.cityCode = cityCode2;
    }
}
