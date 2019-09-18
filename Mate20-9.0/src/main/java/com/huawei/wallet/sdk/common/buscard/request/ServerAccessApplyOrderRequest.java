package com.huawei.wallet.sdk.common.buscard.request;

import com.huawei.wallet.sdk.common.apdu.request.ServerAccessBaseRequest;

public class ServerAccessApplyOrderRequest extends ServerAccessBaseRequest {
    public static final String CURRENCY_CNY = "CNY";
    public static final String SCENE_OPEN_CARD = "0";
    public static final String SCENE_OPEN_CARD_AND_RECHARGE = "2";
    public static final String SCENE_RECHARGE = "1";
    private String actualCardMovePayment;
    private String actualIssuePayment = null;
    private String actualRecharegePayment = null;
    private String appCode;
    private String buCardInfo;
    private String currency = "CNY";
    private String eventId;
    private String partnerId;
    private String scene = null;
    private String theoreticalCardMovePayment;
    private String theoreticalIssuePayment = null;
    private String theoreticalRecharegePayment = null;

    public ServerAccessApplyOrderRequest() {
    }

    public ServerAccessApplyOrderRequest(String issuerId, String cplc, String appletAid, String scene2, String deviceModel, String seChipManuFacturer) {
        setIssuerId(issuerId);
        setCplc(cplc);
        setAppletAid(appletAid);
        setDeviceModel(deviceModel);
        setSeChipManuFacturer(seChipManuFacturer);
        this.scene = scene2;
    }

    public void setScene(String scene2) {
        this.scene = scene2;
    }

    public String getScene() {
        return this.scene;
    }

    public String getActualIssuePayment() {
        return this.actualIssuePayment;
    }

    public void setActualIssuePayment(String actualIssuePayment2) {
        this.actualIssuePayment = actualIssuePayment2;
    }

    public void setTheoreticalIssuePayment(String theoreticalIssuePayment2) {
        this.theoreticalIssuePayment = theoreticalIssuePayment2;
    }

    public String getTheoreticalIssuePayment() {
        return this.theoreticalIssuePayment;
    }

    public String getActualRecharegePayment() {
        return this.actualRecharegePayment;
    }

    public void setActualRecharegePayment(String actualRecharegePayment2) {
        this.actualRecharegePayment = actualRecharegePayment2;
    }

    public String getTheoreticalRecharegePayment() {
        return this.theoreticalRecharegePayment;
    }

    public void setTheoreticalRecharegePayment(String theoreticalRechargePayment) {
        this.theoreticalRecharegePayment = theoreticalRechargePayment;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency2) {
        this.currency = currency2;
    }

    public String getActualCardMovePayment() {
        return this.actualCardMovePayment;
    }

    public void setActualCardMovePayment(String actualCardMovePayment2) {
        this.actualCardMovePayment = actualCardMovePayment2;
    }

    public String getTheoreticalCardMovePayment() {
        return this.theoreticalCardMovePayment;
    }

    public void setTheoreticalCardMovePayment(String theoreticalCardMovePayment2) {
        this.theoreticalCardMovePayment = theoreticalCardMovePayment2;
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId2) {
        this.eventId = eventId2;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setAppCode(String appCode2) {
        this.appCode = appCode2;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public void setPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public String getBuCardInfo() {
        return this.buCardInfo;
    }

    public void setBuCardInfo(String cardInfo) {
        this.buCardInfo = cardInfo;
    }
}
