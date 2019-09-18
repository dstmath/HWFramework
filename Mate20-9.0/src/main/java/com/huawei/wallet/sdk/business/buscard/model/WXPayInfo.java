package com.huawei.wallet.sdk.business.buscard.model;

import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.JsonUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class WXPayInfo {
    private String appId;
    private String nonceStr;
    private String packageValue;
    private String partnerId;
    private String payOrderNo;
    private String prepayId;
    private String sign;
    private String timeStamp;

    private static class Commonkey {
        static final String KEY_APP_ID = "appid";
        static final String KEY_NONCE_STR = "noncestr";
        static final String KEY_PACKAGE_VALUE = "packageValue";
        static final String KEY_PARTNER_ID = "partnerid";
        static final String KEY_PREPAY_ID = "prepayid";
        static final String KEY_SIGN = "sign";
        static final String KEY_TIME_STAMP = "timestamp";

        private Commonkey() {
        }
    }

    public static WXPayInfo build(String json) {
        if (StringUtil.isEmpty(json, true)) {
            return null;
        }
        WXPayInfo payInfo = new WXPayInfo();
        try {
            JSONObject jsonObj = new JSONObject(json);
            payInfo.appId = JsonUtil.getStringValue(jsonObj, "appid");
            if (payInfo.appId == null) {
                return null;
            }
            payInfo.nonceStr = JsonUtil.getStringValue(jsonObj, "noncestr");
            if (payInfo.nonceStr == null) {
                return null;
            }
            payInfo.packageValue = JsonUtil.getStringValue(jsonObj, "packageValue");
            if (payInfo.packageValue == null) {
                return null;
            }
            payInfo.prepayId = JsonUtil.getStringValue(jsonObj, "prepayid");
            if (payInfo.prepayId == null) {
                return null;
            }
            payInfo.sign = JsonUtil.getStringValue(jsonObj, "sign");
            if (payInfo.sign == null) {
                return null;
            }
            payInfo.timeStamp = JsonUtil.getStringValue(jsonObj, "timestamp");
            if (payInfo.timeStamp == null) {
                return null;
            }
            payInfo.partnerId = JsonUtil.getStringValue(jsonObj, "partnerid");
            if (payInfo.partnerId == null) {
                return null;
            }
            return payInfo;
        } catch (JSONException e) {
            LogX.e("PayInfo, JSONException" + e.getMessage(), true);
            return null;
        }
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId2) {
        this.appId = appId2;
    }

    public String getNonceStr() {
        return this.nonceStr;
    }

    public void setNonceStr(String nonceStr2) {
        this.nonceStr = nonceStr2;
    }

    public String getPackageValue() {
        return this.packageValue;
    }

    public void setPackageValue(String packageValue2) {
        this.packageValue = packageValue2;
    }

    public String getPrepayId() {
        return this.prepayId;
    }

    public void setPrepayId(String prepayId2) {
        this.prepayId = prepayId2;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(String timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public void setPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public String getPayOrderNo() {
        return this.payOrderNo;
    }

    public void setPayOrderNo(String payOrderNo2) {
        this.payOrderNo = payOrderNo2;
    }
}
