package com.huawei.wallet.sdk.business.idcard.idcard.server.response;

public class IdCardStatusItem {
    public static final String DEV_STATUS_NORMAL = "0";
    private String QRCode;
    private String aid;
    private String cardName;
    private int cardType;
    private String country;
    private String cplc;
    private String eidCode;
    private String issuerId;
    private String lastModified;
    private String status;
    private String userId;

    public String getQRCode() {
        return this.QRCode;
    }

    public void setQRCode(String QRCode2) {
        this.QRCode = QRCode2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public String getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(String lastModified2) {
        this.lastModified = lastModified2;
    }

    public String getCardName() {
        return this.cardName;
    }

    public void setCardName(String cardName2) {
        this.cardName = cardName2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public int getCardType() {
        return this.cardType;
    }

    public void setCardType(int cardType2) {
        this.cardType = cardType2;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country2) {
        this.country = country2;
    }

    public String getEidCode() {
        return this.eidCode;
    }

    public void setEidCode(String eidCode2) {
        this.eidCode = eidCode2;
    }
}
