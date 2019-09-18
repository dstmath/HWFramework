package com.huawei.wallet.sdk.business.idcard.idcard.server.request;

import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.request.CardServerBaseRequest;

public class ServerAccessBaseRequest extends CardServerBaseRequest {
    public static final String SE_CHIP_MANUFACTURER_HISEE = "02";
    public static final String SE_CHIP_MANUFACTURER_NXP = "01";
    private String aid = null;
    private String cplc = null;
    private String deviceModel = null;
    private String issuerId = null;
    private String phoneManufacturer = null;
    private String reserved = null;
    private String seChipManuFacturer = null;
    private String sn = null;

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
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

    public String getSeChipManuFacturer() {
        return this.seChipManuFacturer;
    }

    public void setSeChipManuFacturer(String seChipManuFacturer2) {
        this.seChipManuFacturer = seChipManuFacturer2;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn2) {
        this.sn = sn2;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setDeviceModel(String deviceModel2) {
        this.deviceModel = deviceModel2;
    }

    public String getPhoneManufacturer() {
        return this.phoneManufacturer;
    }

    public void setPhoneManufacturer(String phoneManufacturer2) {
        this.phoneManufacturer = phoneManufacturer2;
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }
}
