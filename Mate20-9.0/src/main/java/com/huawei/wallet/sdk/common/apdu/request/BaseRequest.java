package com.huawei.wallet.sdk.common.apdu.request;

public class BaseRequest {
    public static final String PAY_TYPE_HUAWEIPAY = "Huaweipay";
    public static final String PAY_TYPE_HUAWEIPAY_UNION = "huaweipayonline";
    public static final String PAY_TYPE_HUAWEIPAY_WALLET = "IAP";
    public static final String PAY_TYPE_WECHAT = "wechat";
    public static final String SE_CHIP_MANUFACTURER_HISEE = "02";
    public static final String SE_CHIP_MANUFACTURER_NXP = "01";
    private String accountUserId = null;
    private String appletAid = null;
    private String basebandVersion = null;
    private String cplc = null;
    private String deviceModel = null;
    private String issuerId = null;
    private String orderId = null;
    private String payType = "Huaweipay";
    private String phoneManufacturer = null;
    private String phoneNumber = null;
    private String reserved = null;
    private String seChipManuFacturer = null;
    private String seCosVersion = null;
    private String sn = null;
    private String systemType = null;
    private String systemVersion = null;
    private String trafficCardId = null;

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }

    public String getSeChipManuFacturer() {
        return this.seChipManuFacturer;
    }

    public void setSeChipManuFacturer(String seChipManuFacturer2) {
        this.seChipManuFacturer = seChipManuFacturer2;
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

    public void setPayType(String payType2) {
        this.payType = payType2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getAccountUserId() {
        return this.accountUserId;
    }

    public void setAccountUserId(String userId) {
        this.accountUserId = userId;
    }

    public void setPhoneNumber(String phoneNumber2) {
        this.phoneNumber = phoneNumber2;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public void setAppletAid(String appletAid2) {
        this.appletAid = appletAid2;
    }

    public String getAppletAid() {
        return this.appletAid;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn2) {
        this.sn = sn2;
    }

    public void setPhoneManufacturer(String phoneManufacturer2) {
        this.phoneManufacturer = phoneManufacturer2;
    }

    public String getPhoneManufacturer() {
        return this.phoneManufacturer;
    }

    public String getTrafficCardId() {
        return this.trafficCardId;
    }

    public void setTrafficCardId(String cardId) {
        this.trafficCardId = cardId;
    }

    public void setBasebandVersion(String basebandVersion2) {
        this.basebandVersion = basebandVersion2;
    }

    public String getBasebandVersion() {
        return this.basebandVersion;
    }

    public String getSystemType() {
        return this.systemType;
    }

    public void setSystemType(String systemType2) {
        this.systemType = systemType2;
    }

    public void setSystemVersion(String systemVersion2) {
        this.systemVersion = systemVersion2;
    }

    public String getSystemVersion() {
        return this.systemVersion;
    }

    public String getSeCosVersion() {
        return this.seCosVersion;
    }

    public void setSeCosVersion(String seCosVersion2) {
        this.seCosVersion = seCosVersion2;
    }

    public void setOrderId(String orderId2) {
        this.orderId = orderId2;
    }

    public String getOrderId() {
        return this.orderId;
    }
}
