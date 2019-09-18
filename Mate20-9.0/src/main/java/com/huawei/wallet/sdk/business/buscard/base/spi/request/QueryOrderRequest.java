package com.huawei.wallet.sdk.business.buscard.base.spi.request;

import com.huawei.wallet.sdk.common.apdu.request.BaseRequest;

public class QueryOrderRequest extends BaseRequest {
    public static final String DEV_QUERY_REFUND = "3";
    public static final String ORDER_STATUS_ABNOMAL = "1";
    public static final String ORDER_STATUS_NOMAL = "0";
    public static final String USER_QUERY_REFUND = "2";
    private String appCode;
    private String orderStatus = null;
    private String orderType;
    private String partnerId;

    public QueryOrderRequest(String issuerId, String cplc, String appletAid, String deviceModel, String seChipManuFacturer) {
        setIssuerId(issuerId);
        setCplc(cplc);
        setAppletAid(appletAid);
        setDeviceModel(deviceModel);
        setSeChipManuFacturer(seChipManuFacturer);
    }

    public void setOrderStatus(String orderStatus2) {
        this.orderStatus = orderStatus2;
    }

    public String getOrderStatus() {
        return this.orderStatus;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public void setAppCode(String appCode2) {
        this.appCode = appCode2;
    }

    public String getOrderType() {
        return this.orderType;
    }

    public void setOrderType(String orderType2) {
        this.orderType = orderType2;
    }
}
