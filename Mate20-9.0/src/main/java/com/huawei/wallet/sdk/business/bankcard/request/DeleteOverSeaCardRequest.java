package com.huawei.wallet.sdk.business.bankcard.request;

import com.huawei.wallet.sdk.common.apdu.request.CardServerBaseRequest;

public class DeleteOverSeaCardRequest extends CardServerBaseRequest {
    private String appletAid;
    private String cplc;
    private String deviceModel;
    private String issuerId;
    private String onlyDeleteApplet;
    private String phoneNumber;
    private String reason;
    private String refId;
    private String refundTicketID;
    private String seChipManuFacturer;
    private String sn;
    private String source;
    private String userId;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getRefId() {
        return this.refId;
    }

    public void setRefId(String refId2) {
        this.refId = refId2;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source2) {
        this.source = source2;
    }

    public String getAppletAid() {
        return this.appletAid;
    }

    public void setAppletAid(String appletAid2) {
        this.appletAid = appletAid2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber2) {
        this.phoneNumber = phoneNumber2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
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

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn2) {
        this.sn = sn2;
    }

    public String getOnlyDeleteApplet() {
        return this.onlyDeleteApplet;
    }

    public void setOnlyDeleteApplet(String onlyDeleteApplet2) {
        this.onlyDeleteApplet = onlyDeleteApplet2;
    }

    public String getRefundTicketID() {
        return this.refundTicketID;
    }

    public void setRefundTicketID(String refundTicketID2) {
        this.refundTicketID = refundTicketID2;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason2) {
        this.reason = reason2;
    }
}
