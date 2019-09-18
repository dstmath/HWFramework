package com.huawei.wallet.sdk.common.apdu.model;

import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessQueryOrder {
    public static final String ORDER_TYPE_OPEN_CARD = "0";
    public static final String ORDER_TYPE_OPEN_CARD_AND_RECHARGE = "2";
    public static final String ORDER_TYPE_RECHARGE = "1";
    public static final String ORDER_TYPE_SHIFT_CARD_IN = "4";
    public static final String ORDER_TYPE_SHIFT_CARD_OUT = "3";
    public static final String ORDER_TYPE_SHIFT_RECHARFE = "5";
    private String amount = null;
    private String cplc = null;
    private String currency = null;
    private String issuerId = null;
    private String orderId = null;
    private String orderTime = null;
    private String orderType = null;
    private String status = null;

    public String getCplc() {
        return this.cplc;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency2) {
        this.currency = currency2;
    }

    public String getOrderTime() {
        return this.orderTime;
    }

    public void setOrderTime(String orderTime2) {
        this.orderTime = orderTime2;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount2) {
        this.amount = amount2;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId2) {
        this.orderId = orderId2;
    }

    public String getOrderType() {
        return this.orderType;
    }

    public void setOrderType(String orderType2) {
        this.orderType = orderType2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public String toString() {
        return "ServerAccessQueryOrder{orderId='" + this.orderId + '\'' + ", orderType='" + this.orderType + '\'' + ", issuerId='" + this.issuerId + '\'' + ", status='" + this.status + '\'' + ", currency='" + this.currency + '\'' + ", orderTime='" + this.orderTime + '\'' + ", amount='" + this.amount + '\'' + '}';
    }

    public static ServerAccessQueryOrder buildFromJson(JSONObject jObject) {
        if (jObject == null) {
            return null;
        }
        ServerAccessQueryOrder order = new ServerAccessQueryOrder();
        try {
            order.orderId = JSONHelper.getStringValue(jObject, "orderNo");
            order.orderType = String.valueOf(JSONHelper.getIntValue(jObject, "orderType"));
            order.issuerId = JSONHelper.getStringValue(jObject, ServerAccessApplyAPDURequest.ReqKey.ISSUERID);
            order.status = JSONHelper.getStringValue(jObject, Constants.FIELD_APPLET_CONFIG_STATUS);
            order.amount = JSONHelper.getStringValue(jObject, "amount");
            order.orderTime = JSONHelper.getStringValue(jObject, "orderTime");
            order.currency = JSONHelper.getStringValue(jObject, "currency");
            order.cplc = JSONHelper.getStringValue(jObject, "cplc");
        } catch (JSONException e) {
            LogX.e("ServerAccessQueryOrder buildFromJson, JSONException");
            order = null;
        }
        return order;
    }
}
