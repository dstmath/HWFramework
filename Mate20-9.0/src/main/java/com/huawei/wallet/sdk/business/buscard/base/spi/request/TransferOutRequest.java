package com.huawei.wallet.sdk.business.buscard.base.spi.request;

import com.huawei.wallet.sdk.common.apdu.request.BaseRequest;

public class TransferOutRequest extends BaseRequest {
    private String appCode;
    private String balance;
    private String eventId;
    private String extend;
    private String partnerId;
    private String transferVerifyFlag;

    public TransferOutRequest(String eventId2, String issueId, String cplc, String appletAid, String seChipManuFacturer, String deviceModel, String cardNo, String balance2) {
        this.eventId = eventId2;
        this.balance = balance2;
        setIssuerId(issueId);
        setCplc(cplc);
        setAppletAid(appletAid);
        setSeChipManuFacturer(seChipManuFacturer);
        setDeviceModel(deviceModel);
        setTrafficCardId(cardNo);
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId2) {
        this.eventId = eventId2;
    }

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(String balance2) {
        this.balance = balance2;
    }

    public String getExtend() {
        return this.extend;
    }

    public void setExtend(String extend2) {
        this.extend = extend2;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public void setPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setAppCode(String appCode2) {
        this.appCode = appCode2;
    }

    public String getTransferVerifyFlag() {
        return this.transferVerifyFlag;
    }

    public void setTransferVerifyFlag(String transferVerifyFlag2) {
        this.transferVerifyFlag = transferVerifyFlag2;
    }
}
