package com.huawei.wallet.sdk.common.apdu.model;

import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.unionpay.tsmservice.data.Constant;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessApplyOrder {
    public static final String ORDER_TYPE_OPEN_CARD = "0";
    public static final String ORDER_TYPE_OPEN_CARD_AND_RECHARGE = "2";
    public static final String ORDER_TYPE_RECHARGE = "1";
    private String SPMerchantId = null;
    private String accessMode = null;
    private String amount = null;
    private String appId = null;
    private String applicationID = null;
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
    private String sdkChannel = null;
    private String serviceCatalog = null;
    private String sign = null;
    private String signType = null;
    private String timeStamp = null;
    private String tn = null;
    private String url = null;
    private String urlVer = null;

    public String getWxPackageValue() {
        return this.packageValue;
    }

    public void setWxPackageValue(String packageValue2) {
        this.packageValue = packageValue2;
    }

    public String getWxAppId() {
        return this.appId;
    }

    public void setWxAppId(String appId2) {
        this.appId = appId2;
    }

    public String getWxNonceStr() {
        return this.nonceStr;
    }

    public void setWxNonceStr(String nonceStr2) {
        this.nonceStr = nonceStr2;
    }

    public String getWxPartnerId() {
        return this.partnerId;
    }

    public void setWxPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public String getWxPrepayId() {
        return this.prepayId;
    }

    public void setWxPrepayId(String prepayId2) {
        this.prepayId = prepayId2;
    }

    public String getWxTimeStamp() {
        return this.timeStamp;
    }

    public void setWxTimeStamp(String timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public String getSdkChannel() {
        return this.sdkChannel;
    }

    public void setSdkChannel(String sdkChannel2) {
        this.sdkChannel = sdkChannel2;
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

    public void setUrlVer(String urlVer2) {
        this.urlVer = urlVer2;
    }

    public String getOrderId() {
        return this.orderId;
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

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName2) {
        this.merchantName = merchantName2;
    }

    public String getApplicationID() {
        return this.applicationID;
    }

    public void setApplicationID(String applicationID2) {
        this.applicationID = applicationID2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getAccessMode() {
        return this.accessMode;
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

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName2) {
        this.productName = productName2;
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

    public void setSignType(String signType2) {
        this.signType = signType2;
    }

    public String getSign() {
        return this.sign;
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

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency2) {
        this.currency = currency2;
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

    public void setOrderTime(String orderTime2) {
        this.orderTime = orderTime2;
    }

    public String getTn() {
        return this.tn;
    }

    public void setTn(String tn2) {
        this.tn = tn2;
    }

    public static ServerAccessApplyOrder buildFromJson(JSONObject jObject) {
        if (jObject == null) {
            return null;
        }
        ServerAccessApplyOrder order = new ServerAccessApplyOrder();
        try {
            order.setOrderId(JSONHelper.getStringValue(jObject, "requestId"));
            order.setOrderType(String.valueOf(JSONHelper.getIntValue(jObject, "orderType")));
            order.setOrderTime(JSONHelper.getStringValue(jObject, "orderTime"));
            order.setSPMerchantId(JSONHelper.getStringValue(jObject, "merchantId"));
            order.setMerchantName(JSONHelper.getStringValue(jObject, Constant.KEY_MERCHANT_NAME));
            order.setApplicationID(JSONHelper.getStringValue(jObject, "applicationID"));
            order.setPackageName(JSONHelper.getStringValue(jObject, Constant.KEY_PACKAGE_NAME));
            order.setAccessMode(JSONHelper.getStringValue(jObject, "accessMode"));
            order.setServiceCatalog(JSONHelper.getStringValue(jObject, SNBConstant.FIELD_SERVICE_CATALOG));
            order.setCurrency(JSONHelper.getStringValue(jObject, "currency"));
            order.setAmount(JSONHelper.getStringValue(jObject, "amount"));
            order.setProductName(JSONHelper.getStringValue(jObject, "productName"));
            order.setProductDesc(JSONHelper.getStringValue(jObject, "productDesc"));
            order.setSignType(JSONHelper.getStringValue(jObject, SNBConstant.FIELD_RSA_SIGN_TYPE));
            order.setSign(JSONHelper.getStringValue(jObject, "sign"));
            order.setUrl(JSONHelper.getStringValue(jObject, "url"));
            order.setUrlVer(JSONHelper.getStringValue(jObject, "urlver"));
            order.setTn(JSONHelper.getStringValue(jObject, "tn"));
            order.setWxAppId(JSONHelper.getStringValue(jObject, "appid"));
            order.setWxNonceStr(JSONHelper.getStringValue(jObject, "noncestr"));
            order.setWxPartnerId(JSONHelper.getStringValue(jObject, "partnerid"));
            order.setWxPrepayId(JSONHelper.getStringValue(jObject, "prepayid"));
            order.setWxTimeStamp(JSONHelper.getStringValue(jObject, "timestamp"));
            order.setWxPackageValue(JSONHelper.getStringValue(jObject, "packageValue"));
            if (jObject.has("sdkChannel")) {
                order.setSdkChannel(JSONHelper.getStringValue(jObject, "sdkChannel"));
            } else {
                order.setSdkChannel("0");
            }
        } catch (JSONException e) {
            LogX.e("ServerAccessApplyOrder buildFromJson, JSONException");
            order = null;
        }
        return order;
    }

    public String toString() {
        return "ServerAccessApplyOrder{orderId='" + this.orderId + '\'' + ", SPMerchantId='" + this.SPMerchantId + '\'' + ", merchantName='" + this.merchantName + '\'' + ", applicationID='" + this.applicationID + '\'' + ", packageName='" + this.packageName + '\'' + ", accessMode='" + this.accessMode + '\'' + ", serviceCatalog='" + this.serviceCatalog + '\'' + ", productName='" + this.productName + '\'' + ", signType='***" + '\'' + ", productDesc='" + this.productDesc + '\'' + ", sign='***" + '\'' + ", amount='" + this.amount + '\'' + ", currency='" + this.currency + '\'' + ", orderType='" + this.orderType + '\'' + ", orderTime='" + this.orderTime + '\'' + ", url='" + this.url + '\'' + ", urlVer='" + this.urlVer + '\'' + ", sdkChannel='" + this.sdkChannel + '\'' + ", tn='" + this.tn + '\'' + ", appId='" + this.appId + '\'' + ", nonceStr='" + this.nonceStr + '\'' + ", partnerId='" + this.partnerId + '\'' + ", prepayId='" + this.prepayId + '\'' + ", timeStamp='" + this.timeStamp + '\'' + ", packageValue='" + this.packageValue + '\'' + '}';
    }
}
