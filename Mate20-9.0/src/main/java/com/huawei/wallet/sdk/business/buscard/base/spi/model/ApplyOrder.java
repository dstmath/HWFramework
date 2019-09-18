package com.huawei.wallet.sdk.business.buscard.base.spi.model;

public class ApplyOrder {
    public static final String ORDER_TYPE_OPEN_CARD = "0";
    public static final String ORDER_TYPE_OPEN_CARD_AND_RECHARGE = "2";
    public static final String ORDER_TYPE_RECHARGE = "1";
    public static final String ORDER_TYPE_TRANSFER_IN = "4";
    public static final String ORDER_TYPE_TRANSFER_IN_RECHARGE = "7";
    public static final String ORDER_TYPE_TRANSFER_OUT = "3";
    private String SPMerchantId = null;
    private String accessMode = null;
    private String amount = null;
    private String appId = null;
    private String applicationID = null;
    private String applyOrderTn = null;
    private String currency = null;
    private String merchantName = null;
    private String nonceStr = null;
    private String orderId = null;
    private String orderTime = null;
    private String orderType = null;
    private String packageName = null;
    private String packageValue = null;
    private String partnerId = null;
    private String prepayId = null;
    private String productDesc = null;
    private String productName = null;
    private String sdkChannel = "0";
    private String serviceCatalog = null;
    private String sign = null;
    private String signType = null;
    private String timeStamp = null;
    private String url = null;
    private String urlVer = null;

    public void setSdkChannel(String sdkChannel2) {
        this.sdkChannel = sdkChannel2;
    }

    public String getSdkChannel() {
        return this.sdkChannel;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getUrlVer() {
        return this.urlVer;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setUrlVer(String urlVer2) {
        this.urlVer = urlVer2;
    }

    public void setOrderId(String orderId2) {
        this.orderId = orderId2;
    }

    public String getSPMerchantId() {
        return this.SPMerchantId;
    }

    public void setSPMerchantId(String merchantId) {
        this.SPMerchantId = merchantId;
    }

    public void setMerchantName(String merchantName2) {
        this.merchantName = merchantName2;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public String getApplicationID() {
        return this.applicationID;
    }

    public void setApplicationID(String applicationId) {
        this.applicationID = applicationId;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getAccessMode() {
        return this.accessMode;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public void setAccessMode(String accessMode2) {
        this.accessMode = accessMode2;
    }

    public String getServiceCatalog() {
        return this.serviceCatalog;
    }

    public void setServiceCatalog(String serviceCatalog2) {
        this.serviceCatalog = serviceCatalog2;
    }

    public void setProductName(String productName2) {
        this.productName = productName2;
    }

    public String getProductName() {
        return this.productName;
    }

    public String getProductDesc() {
        return this.productDesc;
    }

    public void setProductDesc(String productDesc2) {
        this.productDesc = productDesc2;
    }

    public String getSignType() {
        return this.signType;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSignType(String signType2) {
        this.signType = signType2;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount2) {
        this.amount = amount2;
    }

    public void setCurrency(String currency2) {
        this.currency = currency2;
    }

    public String getCurrency() {
        return this.currency;
    }

    public String getOrderType() {
        return this.orderType;
    }

    public void setOrderType(String orderType2) {
        this.orderType = orderType2;
    }

    public String getOrderTime() {
        return this.orderTime;
    }

    public String getApplyOrderTn() {
        return this.applyOrderTn;
    }

    public void setOrderTime(String orderTime2) {
        this.orderTime = orderTime2;
    }

    public void setApplyOrderTn(String applyOrderTn2) {
        this.applyOrderTn = applyOrderTn2;
    }

    public String getWechatPayPackageValue() {
        return this.packageValue;
    }

    public void setWechatPayPackageValue(String packageValue2) {
        this.packageValue = packageValue2;
    }

    public String getWechatPayAppId() {
        return this.appId;
    }

    public void setWechatPayAppId(String appId2) {
        this.appId = appId2;
    }

    public String getWechatPayNonceStr() {
        return this.nonceStr;
    }

    public void setWechatPayNonceStr(String nonceStr2) {
        this.nonceStr = nonceStr2;
    }

    public String getWechatPayPartnerId() {
        return this.partnerId;
    }

    public void setWechatPayPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public String getWechatPayPrepayId() {
        return this.prepayId;
    }

    public void setWechatPayPrepayId(String prepayId2) {
        this.prepayId = prepayId2;
    }

    public String getWechatPayTimeStamp() {
        return this.timeStamp;
    }

    public void setWechatPayTimeStamp(String timeStamp2) {
        this.timeStamp = timeStamp2;
    }
}
