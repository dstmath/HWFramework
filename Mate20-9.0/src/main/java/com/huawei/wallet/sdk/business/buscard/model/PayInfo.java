package com.huawei.wallet.sdk.business.buscard.model;

import com.huawei.wallet.sdk.business.buscard.base.spi.model.ApplyOrder;
import com.huawei.wallet.sdk.business.buscard.base.util.MoneyUtils;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.JsonUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class PayInfo {
    public static final String PAY_RESULT_CODE_CANCLE = "30000";
    public static final String PAY_RESULT_CODE_NET_ERROR = "30005";
    public static final String PAY_RESULT_FAILED = "-1";
    public static final String PAY_RESULT_SUCCESS = "0";
    public static final int PAY_TARGET_BUSCARD_ADD = 100;
    public static final int PAY_TARGET_BUSCARD_RECHARGE = 200;
    private String amount;
    private String applicationID;
    private String currency;
    private boolean hmsPay;
    private String issuerId;
    private boolean nfcRechargeType;
    private String notifyUrl;
    private String productDesc;
    private String productName;
    private String requestId;
    private String sdkChannel;
    private String serviceCatalog;
    private String sign;
    private String signType;
    private String urlVer;
    private String userID;
    private String userName;

    private static class Commonkey {
        static final String KEY_HMS_PAY = "hmsPay";
        static final String KEY_NFC_RECHARGE_TYPE = "nfcRechargeType";
        static final String KEY_NOTIFY_URL = "notifyUrl";
        static final String KEY_ORDER_AMOUNT = "amount";
        static final String KEY_ORDER_APPLICATIONID = "applicationID";
        static final String KEY_ORDER_ISSUERID = "issuerId";
        static final String KEY_ORDER_PACKAGE_NAME = "packageName";
        static final String KEY_ORDER_PRODUCTDESC = "productDesc";
        static final String KEY_ORDER_PRODUCTNAME = "productName";
        static final String KEY_ORDER_REQUESTID = "requestId";
        static final String KEY_ORDER_REQUESTLEDGER = "requestLedger";
        static final String KEY_ORDER_SERVICECATALOG = "serviceCatalog";
        static final String KEY_ORDER_SIGN = "sign";
        static final String KEY_ORDER_SIGNTYPE = "signType";
        static final String KEY_ORDER_SUPPORT_LEDGER_WX = "supportWXLedger";
        static final String KEY_ORDER_USERID = "userID";
        static final String KEY_ORDER_USERNAME = "userName";
        static final String KEY_SDKCHANNEL = "sdkChannel";
        static final String KEY_URLVER = "urlVer";

        private Commonkey() {
        }
    }

    public String getUrlVer() {
        return this.urlVer;
    }

    public void setUrlVer(String urlVer2) {
        this.urlVer = urlVer2;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId2) {
        this.requestId = requestId2;
    }

    public static PayInfo build(String json) {
        return buildForCommon(json);
    }

    public static PayInfo build(ApplyOrder applyOrder) {
        if (applyOrder == null) {
            LogX.e("build PayInfo err, applyOrder is null!");
            return null;
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setRequestId(applyOrder.getOrderId());
        payInfo.amount = applyOrder.getAmount();
        payInfo.applicationID = applyOrder.getApplicationID();
        payInfo.notifyUrl = applyOrder.getUrl();
        payInfo.userName = applyOrder.getMerchantName();
        payInfo.userID = applyOrder.getSPMerchantId();
        payInfo.signType = applyOrder.getSignType();
        payInfo.sign = applyOrder.getSign();
        payInfo.serviceCatalog = applyOrder.getServiceCatalog();
        payInfo.productName = applyOrder.getProductName();
        payInfo.productDesc = applyOrder.getProductDesc();
        payInfo.urlVer = applyOrder.getUrlVer();
        payInfo.currency = applyOrder.getCurrency();
        payInfo.sdkChannel = applyOrder.getSdkChannel();
        return payInfo;
    }

    public static PayInfo buildForSNB(String json) {
        if (StringUtil.isEmpty(json, true)) {
            return null;
        }
        PayInfo payInfo = new PayInfo();
        try {
            JSONObject jsonObj = new JSONObject(json);
            payInfo.userName = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_USER_NAME);
            payInfo.userID = JsonUtil.getStringValue(jsonObj, "user_id");
            payInfo.applicationID = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_APP_ID);
            payInfo.productName = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_PRODUCT_NAME);
            String tempAmount = JsonUtil.getStringValue(jsonObj, "amount");
            if (!StringUtil.isEmpty(tempAmount, true)) {
                payInfo.amount = MoneyUtils.getFormatAmount(tempAmount);
            }
            payInfo.productDesc = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_PRODUCT_DESC);
            payInfo.requestId = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_ORDER_ID);
            payInfo.serviceCatalog = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_SERVICE_CATALOG);
            payInfo.sign = JsonUtil.getStringValue(jsonObj, "sign");
            payInfo.signType = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_RSA_SIGN_TYPE);
            payInfo.notifyUrl = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_NOTIFY_URL);
            return payInfo;
        } catch (JSONException e) {
            LogX.e("PayInfo, JSONException" + e.getMessage(), true);
            return null;
        }
    }

    private static PayInfo buildForCommon(String json) {
        if (StringUtil.isEmpty(json, true)) {
            return null;
        }
        PayInfo payInfo = new PayInfo();
        try {
            JSONObject jsonObj = new JSONObject(json);
            payInfo.userName = JsonUtil.getStringValue(jsonObj, "userName");
            if (payInfo.userName == null) {
                return null;
            }
            payInfo.userID = JsonUtil.getStringValue(jsonObj, "userID");
            if (payInfo.userID == null) {
                return null;
            }
            payInfo.applicationID = JsonUtil.getStringValue(jsonObj, "applicationID");
            if (payInfo.applicationID == null) {
                return null;
            }
            payInfo.productName = JsonUtil.getStringValue(jsonObj, "productName");
            if (payInfo.productName == null) {
                return null;
            }
            payInfo.amount = MoneyUtils.getFormatAmount(JsonUtil.getStringValue(jsonObj, "amount"));
            if (payInfo.amount == null) {
                return null;
            }
            payInfo.productDesc = JsonUtil.getStringValue(jsonObj, "productDesc");
            if (payInfo.productDesc == null) {
                return null;
            }
            payInfo.requestId = JsonUtil.getStringValue(jsonObj, "requestId");
            if (payInfo.requestId == null) {
                return null;
            }
            payInfo.serviceCatalog = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_SERVICE_CATALOG);
            if (payInfo.serviceCatalog == null) {
                return null;
            }
            payInfo.sign = JsonUtil.getStringValue(jsonObj, "sign");
            if (payInfo.sign == null) {
                return null;
            }
            payInfo.signType = JsonUtil.getStringValue(jsonObj, SNBConstant.FIELD_RSA_SIGN_TYPE);
            if (payInfo.signType == null) {
                return null;
            }
            payInfo.notifyUrl = JsonUtil.getStringValue(jsonObj, "notifyUrl");
            if (payInfo.notifyUrl == null) {
                return null;
            }
            return payInfo;
        } catch (JSONException e) {
            LogX.e("PayInfo, JSONException" + e.getMessage(), true);
            return null;
        }
    }

    public boolean isHmsPay() {
        return this.hmsPay;
    }

    public void setHmsPay(boolean hmsPay2) {
        this.hmsPay = hmsPay2;
    }

    public boolean isNfcRechargeType() {
        return this.nfcRechargeType;
    }

    public void setNfcRechargeType(boolean nfcRechargeType2) {
        this.nfcRechargeType = nfcRechargeType2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }
}
