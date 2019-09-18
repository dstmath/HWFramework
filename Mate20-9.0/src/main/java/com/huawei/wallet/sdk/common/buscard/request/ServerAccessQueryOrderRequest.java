package com.huawei.wallet.sdk.common.buscard.request;

import com.huawei.wallet.sdk.common.apdu.request.ServerAccessBaseRequest;

public class ServerAccessQueryOrderRequest extends ServerAccessBaseRequest {
    public static final String ORDER_STATUS_ABNOMAL = "1";
    public static final String ORDER_STATUS_NOMAL = "0";
    private String appCode;
    private String orderStatus = null;
    private String orderType;
    private String partnerId;

    public ServerAccessQueryOrderRequest(String issuerId, String cplc, String appletAId, String deviceModel, String seChipManuFacturer) {
        setIssuerId(issuerId);
        setCplc(cplc);
        setAppletAid(appletAId);
        setDeviceModel(deviceModel);
        setSeChipManuFacturer(seChipManuFacturer);
    }

    public String getOrderStatus() {
        return this.orderStatus;
    }

    public void setOrderStatus(String orderStatus2) {
        this.orderStatus = orderStatus2;
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

    public void setOrderType(String orderType2) {
        this.orderType = orderType2;
    }

    public String getOrderType() {
        return this.orderType;
    }
}
